import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Home.css";

function Home() {
  const videoUrl = process.env.REACT_APP_HOME_VIDEO_URL;

  const [isBooting, setIsBooting] = useState(false);
  const [bootProgress, setBootProgress] = useState(0);
  const [bootText, setBootText] = useState("");
  const navigate = useNavigate();

  const handlePowerButton = () => {
    setIsBooting(true);

    // 부팅 텍스트 타이핑 효과
    const text = "OPTICORE SYSTEM BOOTING";
    let index = 0;
    const typingInterval = setInterval(() => {
      if (index < text.length) {
        setBootText(text.slice(0, index + 1));
        index++;
      } else {
        clearInterval(typingInterval);
      }
    }, 80);

    // 프로그레스 바 애니메이션
    const progressInterval = setInterval(() => {
      setBootProgress((prev) => {
        if (prev >= 100) {
          clearInterval(progressInterval);
          // 부팅 완료 후 페이지 전환
          setTimeout(() => {
            navigate("/home2");
          }, 500);
          return 100;
        }
        return prev + 2;
      });
    }, 40);
  };

  return (
    <div className="home-container">
      <video
        autoPlay
        loop
        muted
        id="video-background"
        className="video-background"
      >
        <source src={videoUrl || ""} type="video/mp4" />
        당신의 브라우저는 비디오 태그를 지원하지 않습니다.
      </video>

      <div className="content-overlay">
        {!isBooting ? (
          <button className="power-button" onClick={handlePowerButton}>
            <div className="power-icon">
              <div className="power-circle"></div>
              <div className="power-line"></div>
            </div>
          </button>
        ) : null}
      </div>

      {/* 부팅 스크린 오버레이 */}
      {isBooting && (
        <div className="boot-screen">
          <div className="boot-content">
            {/* <div className="boot-text">{bootText}_</div> */}
            <div className="progress-container">
              <div
                className="progress-bar"
                style={{ width: `${bootProgress}%` }}
              ></div>
            </div>
            <div className="boot-percentage">{bootProgress}%</div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Home;
