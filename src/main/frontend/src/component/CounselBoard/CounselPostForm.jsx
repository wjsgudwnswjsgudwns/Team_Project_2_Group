import { useRef, useEffect, useState } from "react";
import api from "../../api/axiosConfig";

export default function CounselPostForm({
  formData,
  isEditing,
  onFormChange,
  onSubmit,
  onCancel,
}) {
  const contentEditableRef = useRef(null);
  const fileInputRef = useRef(null);
  const isComposingRef = useRef(false);

  const [localTitle, setLocalTitle] = useState(formData.cTitle);
  const [showVideoModal, setShowVideoModal] = useState(false);
  const [videoUrl, setVideoUrl] = useState("");

  useEffect(() => {
    setLocalTitle(formData.cTitle);
  }, [formData.cTitle]);

  useEffect(() => {
    if (contentEditableRef.current) {
      if (contentEditableRef.current.innerHTML !== formData.cContent) {
        contentEditableRef.current.innerHTML = formData.cContent || "";
      }
    }
  }, [formData.cContent, isEditing]);

  const handleImageInsert = async (e) => {
    const files = Array.from(e.target.files);

    for (const file of files) {
      if (file.size > 5 * 1024 * 1024) {
        alert(`${file.name}은(는) 5MB를 초과합니다.`);
        continue;
      }

      const formData = new FormData();
      formData.append("file", file);

      try {
        const response = await api.post("/api/image/upload", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });

        const imageUrl = response.data.url;

        const img = document.createElement("img");
        img.src = imageUrl;
        img.style.maxWidth = "100%";
        img.style.height = "auto";
        img.style.margin = "10px 0";

        if (contentEditableRef.current) {
          contentEditableRef.current.focus();
          const selection = window.getSelection();
          if (selection.rangeCount > 0) {
            const range = selection.getRangeAt(0);
            range.deleteContents();
            range.insertNode(img);
            range.setStartAfter(img);
            range.setEndAfter(img);
            selection.removeAllRanges();
            selection.addRange(range);
          }

          onFormChange({
            cTitle: localTitle,
            cContent: contentEditableRef.current.innerHTML,
          });
        }
      } catch (error) {
        alert("이미지 업로드 실패: " + error.message);
      }
    }

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const convertToEmbedUrl = (url) => {
    const youtubeRegex =
      /(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
    const youtubeMatch = url.match(youtubeRegex);
    if (youtubeMatch) {
      return `https://www.youtube.com/embed/${youtubeMatch[1]}`;
    }

    const vimeoRegex = /vimeo\.com\/(\d+)/;
    const vimeoMatch = url.match(vimeoRegex);
    if (vimeoMatch) {
      return `https://player.vimeo.com/video/${vimeoMatch[1]}`;
    }

    return null;
  };

  const handleVideoInsert = () => {
    if (!videoUrl.trim()) {
      alert("동영상 URL을 입력해주세요.");
      return;
    }

    const embedUrl = convertToEmbedUrl(videoUrl);
    if (!embedUrl) {
      alert("지원하는 형식: YouTube, Vimeo URL을 입력해주세요.");
      return;
    }

    const videoContainer = document.createElement("div");
    videoContainer.className = "video-container";
    videoContainer.style.position = "relative";
    videoContainer.style.paddingBottom = "56.25%";
    videoContainer.style.height = "0";
    videoContainer.style.overflow = "hidden";
    videoContainer.style.margin = "15px 0";

    const iframe = document.createElement("iframe");
    iframe.src = embedUrl;
    iframe.style.position = "absolute";
    iframe.style.top = "0";
    iframe.style.left = "0";
    iframe.style.width = "100%";
    iframe.style.height = "100%";
    iframe.setAttribute("frameborder", "0");
    iframe.setAttribute("allowfullscreen", "true");

    videoContainer.appendChild(iframe);

    if (contentEditableRef.current) {
      contentEditableRef.current.focus();

      const selection = window.getSelection();
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        range.deleteContents();
        range.insertNode(videoContainer);

        range.setStartAfter(videoContainer);
        range.setEndAfter(videoContainer);
        selection.removeAllRanges();
        selection.addRange(range);
      }

      onFormChange({
        cTitle: localTitle,
        cContent: contentEditableRef.current.innerHTML,
      });
    }

    setVideoUrl("");
    setShowVideoModal(false);
  };

  const handleContentChange = () => {
    if (isComposingRef.current) return;

    if (contentEditableRef.current) {
      onFormChange({
        cTitle: localTitle,
        cContent: contentEditableRef.current.innerHTML,
      });
    }
  };

  const handleCompositionStart = () => {
    isComposingRef.current = true;
  };

  const handleCompositionEnd = () => {
    isComposingRef.current = false;
    if (contentEditableRef.current) {
      onFormChange({
        cTitle: localTitle,
        cContent: contentEditableRef.current.innerHTML,
      });
    }
  };

  const handleTitleChange = (e) => {
    const newTitle = e.target.value;
    setLocalTitle(newTitle);

    onFormChange({
      cTitle: newTitle,
      cContent: contentEditableRef.current?.innerHTML || formData.cContent,
    });
  };

  return (
    <div className="write-form">
      <input
        type="text"
        placeholder="제목"
        value={localTitle}
        onChange={handleTitleChange}
        className="title-input"
      />

      <div className="image-insert-area">
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="image-insert-btn"
        >
          📷 이미지 삽입
        </button>
        <button
          type="button"
          onClick={() => setShowVideoModal(true)}
          className="video-insert-btn"
        >
          🎥 동영상 삽입
        </button>
        <span className="image-insert-note">
          커서 위치에 미디어가 삽입됩니다
        </span>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/*"
        onChange={handleImageInsert}
        style={{ display: "none" }}
      />

      <div
        ref={contentEditableRef}
        contentEditable
        onInput={handleContentChange}
        onCompositionStart={handleCompositionStart}
        onCompositionEnd={handleCompositionEnd}
        placeholder="내용을 입력하세요..."
        className="content-editor"
        suppressContentEditableWarning
      />

      <div className="form-buttons">
        <button onClick={onSubmit} className="form-submit-btn">
          {isEditing ? "수정하기" : "작성하기"}
        </button>
        {isEditing && onCancel && (
          <button onClick={onCancel} className="form-cancel-btn">
            취소
          </button>
        )}
      </div>

      {showVideoModal && (
        <div
          className="video-modal-overlay"
          onClick={() => setShowVideoModal(false)}
        >
          <div className="video-modal" onClick={(e) => e.stopPropagation()}>
            <h3>동영상 삽입</h3>
            <p>YouTube 또는 Vimeo URL을 입력하세요</p>
            <input
              type="text"
              placeholder="https://www.youtube.com/watch?v=..."
              value={videoUrl}
              onChange={(e) => setVideoUrl(e.target.value)}
              className="video-url-input"
              onKeyPress={(e) => {
                if (e.key === "Enter") handleVideoInsert();
              }}
            />
            <div className="video-modal-buttons">
              <button
                onClick={handleVideoInsert}
                className="video-modal-insert-btn"
              >
                삽입
              </button>
              <button
                onClick={() => {
                  setShowVideoModal(false);
                  setVideoUrl("");
                }}
                className="video-modal-cancel-btn"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
