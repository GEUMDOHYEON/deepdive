/**
 * DeepDive AI 응답 지연 측정 테스트
 *
 * 목적: AI API 호출이 포함된 두 엔드포인트의 응답 시간을 측정한다.
 *   - POST /api/v1/interviews/sessions       : 첫 질문 생성 (AI 1회 호출)
 *   - POST /api/v1/interviews/sessions/{id}/questions/{qid}/answers : 답변 평가 + 다음 질문 생성 (AI 1회 호출)
 *
 * 실행:
 *   k6 run k6/ai-latency-test.js
 *
 * 환경 변수 (필요 시 오버라이드):
 *   k6 run -e BASE_URL=http://localhost:8080 -e EMAIL=test@test.com -e PASSWORD=test1234! k6/ai-latency-test.js
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";

// ── 커스텀 메트릭 ──────────────────────────────────────────────
var sessionStartDuration = new Trend("ai_session_start_ms", true);
var answerSubmitDuration  = new Trend("ai_answer_submit_ms", true);
var errorRate = new Rate("error_rate");

// ── 설정 ───────────────────────────────────────────────────────
var BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
var EMAIL    = __ENV.EMAIL    || "test@deepdive.com";
var PASSWORD = __ENV.PASSWORD || "test1234!";

export var options = {
  scenarios: {
    single_user: {
      executor: "per-vu-iterations",
      vus: 1,
      iterations: 3,
      maxDuration: "5m",
    },
  },
  thresholds: {
    "ai_session_start_ms": ["p(95)<15000"],
    "ai_answer_submit_ms": ["p(95)<20000"],
    "error_rate": ["rate<0.1"],
  },
};

// ── 헬퍼 ───────────────────────────────────────────────────────
function jsonHeaders(cookies) {
  var headers = { "Content-Type": "application/json" };
  if (cookies) { headers["Cookie"] = cookies; }
  return { headers: headers };
}

function extractCookies(response) {
  var raw = response.headers["Set-Cookie"] || "";
  return raw
    .split(",")
    .map(function(c) { return c.trim().split(";")[0]; })
    .filter(Boolean)
    .join("; ");
}

function safeGet(obj, key) {
  return obj && obj[key] != null ? obj[key] : null;
}

// ── 메인 시나리오 ───────────────────────────────────────────────
export default function () {
  // 1. 로그인
  var loginRes = http.post(
    BASE_URL + "/api/v1/auth/login",
    JSON.stringify({ email: EMAIL, password: PASSWORD }),
    jsonHeaders(null)
  );

  var loginOk = check(loginRes, {
    "login 200": function(r) { return r.status === 200; },
  });
  errorRate.add(!loginOk);

  if (!loginOk) {
    console.error("로그인 실패: " + loginRes.status + " " + loginRes.body);
    return;
  }

  var cookies = extractCookies(loginRes);

  // 2. 세션 시작 (첫 질문 생성 — AI 호출)
  var startTime = Date.now();
  var sessionRes = http.post(
    BASE_URL + "/api/v1/interviews/sessions",
    JSON.stringify({ category: "OS" }),
    jsonHeaders(cookies)
  );
  var startMs = Date.now() - startTime;
  sessionStartDuration.add(startMs);

  var sessionBody = JSON.parse(sessionRes.body);
  var sessionData = sessionBody.data || {};

  var sessionOk = check(sessionRes, {
    "session start 201": function(r) { return r.status === 201; },
    "has sessionId":     function() { return safeGet(sessionData, "sessionId") !== null; },
    "has firstQuestion": function() { return safeGet(sessionData, "firstQuestion") !== null; },
  });
  errorRate.add(!sessionOk);

  if (!sessionOk) {
    console.error("세션 시작 실패: " + sessionRes.status + " " + sessionRes.body);
    return;
  }

  var sessionId  = sessionData.sessionId;
  var firstQ     = sessionData.firstQuestion || {};
  var questionId = firstQ.questionId;

  console.log("[세션 시작] sessionId=" + sessionId + " | 소요=" + startMs + "ms | 질문=\"" + firstQ.content + "\"");

  sleep(1);

  // 3. 답변 제출 (AI 평가 + 다음 질문 — 가장 무거운 구간)
  var sampleAnswer =
    "프로세스는 독립적인 메모리 공간을 가지는 실행 단위이고, " +
    "스레드는 프로세스 내에서 코드, 데이터, 힙 영역을 공유하며 실행되는 단위입니다. " +
    "컨텍스트 스위칭 비용은 스레드가 더 낮고, 스레드 간 통신은 공유 메모리를 사용합니다.";

  var answerStart = Date.now();
  var answerRes = http.post(
    BASE_URL + "/api/v1/interviews/sessions/" + sessionId + "/questions/" + questionId + "/answers",
    JSON.stringify({ content: sampleAnswer, processingTime: 30 }),
    jsonHeaders(cookies)
  );
  var answerMs = Date.now() - answerStart;
  answerSubmitDuration.add(answerMs);

  var answerBody = JSON.parse(answerRes.body);
  var answerData = answerBody.data || {};
  var feedback   = answerData.feedback || {};

  var answerOk = check(answerRes, {
    "answer submit 200": function(r) { return r.status === 200; },
    "has feedback":      function()  { return safeGet(answerData, "feedback") !== null; },
  });
  errorRate.add(!answerOk);

  if (!answerOk) {
    console.error("답변 제출 실패: " + answerRes.status + " " + answerRes.body);
    return;
  }

  console.log(
    "[답변 제출] 소요=" + answerMs + "ms | " +
    "정확성=" + feedback.scoreAccuracy + " | " +
    "논리=" + feedback.scoreLogic + " | " +
    "세션완료=" + answerData.sessionCompleted
  );

  sleep(2);
}

export function handleSummary(data) {
  function fmt(metrics) {
    if (!metrics) { return "데이터 없음"; }
    var v = metrics.values || {};
    return (
      "avg=" + Math.round(v.avg || 0) + "ms  " +
      "p50=" + Math.round(v["p(50)"] || 0) + "ms  " +
      "p95=" + Math.round(v["p(95)"] || 0) + "ms  " +
      "max=" + Math.round(v.max || 0) + "ms"
    );
  }

  var errRate = data.metrics["error_rate"];
  var errPct  = errRate ? ((errRate.values.rate || 0) * 100).toFixed(1) : "0.0";

  var summary = [
    "========================================",
    "  DeepDive AI 응답 지연 측정 결과",
    "========================================",
    "  [첫 질문 생성]  " + fmt(data.metrics["ai_session_start_ms"]),
    "  [답변 평가]     " + fmt(data.metrics["ai_answer_submit_ms"]),
    "  [에러율]        " + errPct + "%",
    "========================================",
  ].join("\n");

  console.log(summary);
  return { "k6/ai-latency-result.txt": summary };
}
