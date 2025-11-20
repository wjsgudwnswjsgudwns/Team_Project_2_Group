import { useState } from "react";
import api from "../api/axiosConfig";
import "./HelpAnswer.css";

function HelpAnswer({ helpId, existingAnswer, onAnswerUpdate }) {
  const [isEditing, setIsEditing] = useState(false);
  const [answerContent, setAnswerContent] = useState(
    existingAnswer?.answer || ""
  );
  const [loading, setLoading] = useState(false);

  // 답변 작성/수정
  const handleSubmitAnswer = async (e) => {
    e.preventDefault();

    if (!answerContent.trim()) {
      alert("답변 내용을 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const token = localStorage.getItem("token");

      await api.post(
        `/api/help/${helpId}/answer`,
        { answer: answerContent },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      alert("답변이 등록되었습니다.");
      setIsEditing(false);

      // 부모 컴포넌트에 답변 업데이트 알림
      if (onAnswerUpdate) {
        onAnswerUpdate();
      }
    } catch (error) {
      console.error("답변 등록 실패:", error);
      alert(error.response?.data || "답변 등록 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 답변 삭제
  const handleDeleteAnswer = async () => {
    if (!window.confirm("답변을 삭제하시겠습니까?")) {
      return;
    }

    try {
      setLoading(true);
      const token = localStorage.getItem("token");

      await api.delete(`/api/help/${helpId}/answer`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      alert("답변이 삭제되었습니다.");
      setAnswerContent("");
      setIsEditing(false);

      // 부모 컴포넌트에 답변 업데이트 알림
      if (onAnswerUpdate) {
        onAnswerUpdate();
      }
    } catch (error) {
      console.error("답변 삭제 실패:", error);
      alert(error.response?.data || "답변 삭제 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 답변이 없는 경우 - 답변 작성 모드
  if (!existingAnswer && !isEditing) {
    return (
      <div className="answer-section">
        <button className="write-answer-btn" onClick={() => setIsEditing(true)}>
          답변 작성하기
        </button>
      </div>
    );
  }

  // 답변 작성/수정 폼
  if (isEditing) {
    return (
      <div className="answer-section">
        <div className="answer-form">
          <h3>답변 작성</h3>
          <form onSubmit={handleSubmitAnswer}>
            <textarea
              className="form-textarea"
              placeholder="답변 내용을 입력해주세요."
              value={answerContent}
              onChange={(e) => setAnswerContent(e.target.value)}
              rows="8"
              disabled={loading}
            />
            <div className="answer-form-actions">
              <button
                type="submit"
                className="answer-submit-btn"
                disabled={loading}
              >
                {loading ? "등록 중..." : "답변 등록"}
              </button>
              <button
                type="button"
                className="answer-cancel-btn"
                onClick={() => {
                  setIsEditing(false);
                  setAnswerContent(existingAnswer?.answer || "");
                }}
                disabled={loading}
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  // 답변이 있는 경우 - 답변 표시
  return (
    <div className="answer-section">
      <div className="answer-display">
        <div className="answer-header">
          <h3>관리자 답변</h3>
          <div className="answer-meta">
            <span className="answer-author">
              {existingAnswer.admin?.nickname || "관리자"}
            </span>
            {existingAnswer.answeredDate && (
              <span className="answer-date">
                {new Date(existingAnswer.answeredDate).toLocaleDateString(
                  "ko-KR",
                  {
                    year: "numeric",
                    month: "2-digit",
                    day: "2-digit",
                    hour: "2-digit",
                    minute: "2-digit",
                  }
                )}
              </span>
            )}
          </div>
        </div>
        <div className="answer-content">
          <p>{existingAnswer.answer}</p>
        </div>
        <div className="answer-actions">
          <button
            className="edit-answer-btn"
            onClick={() => setIsEditing(true)}
            disabled={loading}
          >
            수정
          </button>
          <button
            className="delete-answer-btn"
            onClick={handleDeleteAnswer}
            disabled={loading}
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  );
}

export default HelpAnswer;
