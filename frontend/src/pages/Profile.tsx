import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Loader2, Pencil, Check, X, Trash2 } from "lucide-react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useAuth } from "@/contexts/AuthContext";
import { memberApi } from "@/api/member";

const Profile = () => {
  const navigate = useNavigate();
  const { user, setUser, logout } = useAuth();

  const [isEditing, setIsEditing] = useState(false);
  const [nicknameInput, setNicknameInput] = useState(user?.nickname ?? "");
  const [isSaving, setIsSaving] = useState(false);
  const [nicknameError, setNicknameError] = useState("");

  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const startEdit = () => {
    setNicknameInput(user?.nickname ?? "");
    setNicknameError("");
    setIsEditing(true);
  };

  const cancelEdit = () => {
    setIsEditing(false);
    setNicknameError("");
  };

  const saveNickname = async () => {
    const trimmed = nicknameInput.trim();
    if (!trimmed) {
      setNicknameError("닉네임을 입력해주세요.");
      return;
    }
    if (trimmed === user?.nickname) {
      setIsEditing(false);
      return;
    }
    setIsSaving(true);
    try {
      const res = await memberApi.updateNickname(trimmed);
      const updated = res.data.data;
      setUser(updated);
      localStorage.setItem("user", JSON.stringify(updated));
      setIsEditing(false);
    } catch (err: any) {
      setNicknameError(err.response?.data?.error ?? "닉네임 변경에 실패했습니다.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    setIsDeleting(true);
    try {
      await memberApi.deleteMe();
      await logout();
      navigate("/login");
    } finally {
      setIsDeleting(false);
    }
  };

  if (!user) return null;

  const initials = user.nickname.slice(0, 2).toUpperCase();

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />

      <main className="flex-1 pt-24 pb-16">
        <div className="container max-w-lg">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="space-y-4"
          >
            {/* 프로필 카드 */}
            <div className="p-6 rounded-2xl border border-border bg-background">
              {/* 아바타 */}
              <div className="flex items-center gap-4 mb-6">
                <div className="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                  <span className="text-lg font-display font-bold text-primary">{initials}</span>
                </div>
                <div>
                  <p className="font-display font-semibold text-base text-foreground">{user.nickname}</p>
                  <p className="text-sm text-muted-foreground">{user.email}</p>
                </div>
              </div>

              {/* 구분선 */}
              <div className="border-t border-border mb-5" />

              {/* 이메일 */}
              <div className="mb-4">
                <p className="text-xs font-medium text-muted-foreground mb-1.5">이메일</p>
                <p className="text-sm text-foreground bg-secondary px-3 py-2.5 rounded-lg">
                  {user.email}
                </p>
              </div>

              {/* 닉네임 */}
              <div>
                <p className="text-xs font-medium text-muted-foreground mb-1.5">닉네임</p>
                {isEditing ? (
                  <div className="space-y-1.5">
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={nicknameInput}
                        onChange={(e) => setNicknameInput(e.target.value)}
                        maxLength={20}
                        autoFocus
                        onKeyDown={(e) => {
                          if (e.key === "Enter") saveNickname();
                          if (e.key === "Escape") cancelEdit();
                        }}
                        className="flex-1 px-3 py-2 rounded-lg bg-background border border-primary focus:ring-1 focus:ring-primary outline-none text-sm text-foreground transition-all"
                      />
                      <button
                        onClick={saveNickname}
                        disabled={isSaving}
                        className="flex items-center justify-center w-9 h-9 rounded-lg bg-primary text-primary-foreground hover:opacity-90 transition-opacity disabled:opacity-50 shrink-0"
                      >
                        {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4" />}
                      </button>
                      <button
                        onClick={cancelEdit}
                        disabled={isSaving}
                        className="flex items-center justify-center w-9 h-9 rounded-lg bg-secondary border border-border hover:bg-surface-hover transition-colors shrink-0"
                      >
                        <X className="w-4 h-4 text-muted-foreground" />
                      </button>
                    </div>
                    {nicknameError && (
                      <p className="text-xs text-destructive">{nicknameError}</p>
                    )}
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <p className="flex-1 text-sm text-foreground bg-secondary px-3 py-2.5 rounded-lg">
                      {user.nickname}
                    </p>
                    <button
                      onClick={startEdit}
                      className="flex items-center justify-center w-9 h-9 rounded-lg bg-secondary border border-border hover:bg-surface-hover transition-colors shrink-0"
                      title="닉네임 수정"
                    >
                      <Pencil className="w-4 h-4 text-muted-foreground" />
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* 위험 구역 */}
            <div className="p-5 rounded-2xl border border-destructive/20 bg-background">
              <p className="text-xs font-medium text-muted-foreground mb-3">위험 구역</p>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground">회원 탈퇴</p>
                  <p className="text-xs text-muted-foreground mt-0.5">
                    모든 면접 기록이 영구 삭제됩니다.
                  </p>
                </div>
                <button
                  onClick={() => setShowDeleteDialog(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-destructive/30 text-destructive text-xs font-medium hover:bg-destructive/5 transition-colors shrink-0 ml-4"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  탈퇴
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      </main>

      <Footer />

      {/* 회원 탈퇴 확인 다이얼로그 */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>정말 탈퇴하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>
              계정과 모든 면접 기록이 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={isDeleting}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {isDeleting && <Loader2 className="w-4 h-4 animate-spin mr-1.5" />}
              탈퇴하기
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Profile;
