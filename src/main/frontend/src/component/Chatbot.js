import React, { useState, useRef, useEffect } from "react";
import "./Chatbot.css";
import api from "../api/axiosConfig";

import {
  initialResponses,
  initialMessage,
  stepQuestions,
} from "./chatbotResponses";

const Chatbot = () => {
  const chatbot = process.env.REACT_APP_CHATBOT_URL;

  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([initialMessage]);
  const [inputMessage, setInputMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [selectedOption, setSelectedOption] = useState(null);
  const [currentStep, setCurrentStep] = useState(null);
  const [collectedData, setCollectedData] = useState({});
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  const handleOptionClick = async (option) => {
    // 옵션 버튼 클릭 시 해당 메시지의 options 제거
    setMessages((prev) =>
      prev.map((msg, index) =>
        index === prev.length - 1 && msg.options
          ? { ...msg, options: undefined }
          : msg
      )
    );

    const userMessage = { role: "user", content: option.text };
    setMessages((prev) => [...prev, userMessage]);

    if (!selectedOption) {
      setSelectedOption(option.id);
    }

    setCurrentStep(option.id);

    const predefinedResponse = initialResponses[option.id];

    if (predefinedResponse) {
      const aiMessage = {
        role: "assistant",
        content: predefinedResponse.content,
        options: predefinedResponse.options,
      };
      setMessages((prev) => [...prev, aiMessage]);

      if (predefinedResponse.useApi) {
        setIsLoading(true);
        try {
          const response = await api.post("/api/chat/message", {
            message: option.text,
          });
          const apiMessage = {
            role: "assistant",
            content: response.data.reply,
          };
          setMessages((prev) => [...prev, apiMessage]);
        } catch (error) {
          console.error("Error:", error);
          const errorMessage = {
            role: "assistant",
            content: "오류가 발생했습니다.",
          };
          setMessages((prev) => [...prev, errorMessage]);
        } finally {
          setIsLoading(false);
        }
      }
    }
  };

  const sendMessage = async () => {
    if (!inputMessage.trim()) return;

    const userMessage = { role: "user", content: inputMessage };
    setMessages([...messages, userMessage]);

    const currentInput = inputMessage;
    setInputMessage("");

    // 1-2 단계: CPU 입력
    if (currentStep === "1-2") {
      setCollectedData({ cpu: currentInput });
      setCurrentStep("1-2-gpu");

      const nextQuestion = stepQuestions["1-2-gpu"];
      const aiMessage = {
        role: "assistant",
        content: nextQuestion.content,
      };
      setMessages((prev) => [...prev, aiMessage]);
      return;
    }

    // 단계별 질문 처리
    if (stepQuestions[currentStep]) {
      const stepConfig = stepQuestions[currentStep];

      // 데이터 저장
      const updatedData = {
        ...collectedData,
        [stepConfig.dataKey]: currentInput,
      };
      setCollectedData(updatedData);

      // 완료 단계인지 확인
      if (stepConfig.nextStep === "complete") {
        // 모든 데이터 수집 완료, API 호출
        setIsLoading(true);
        try {
          const fullSpec = `[중고 PC 가격 확인 상담]
            - CPU: ${updatedData.cpu}
            - GPU: ${updatedData.gpu}
            - RAM: ${updatedData.ram}
            - SSD: ${updatedData.ssd}
            - 쿨러: ${updatedData.cooler}
            - 메인보드: ${updatedData.mainboard}
            - 케이스: ${updatedData.case}
            - 파워: ${updatedData.power}
            - 구매 가격: ${updatedData.price}

            구매 가격이 합리적인 PC인지 판별해주세요.`;

          const response = await api.post("/api/chat/message", {
            message: fullSpec,
          });

          const apiMessage = {
            role: "assistant",
            content: response.data.reply,
          };
          setMessages((prev) => [...prev, apiMessage]);

          // 처음으로 돌아가기 - 초기 메시지 추가
          setTimeout(() => {
            setMessages((prev) => [...prev, initialMessage]);
          }, 500);

          // 초기화
          setCurrentStep(null);
          setSelectedOption(null);
          setCollectedData({});
        } catch (error) {
          console.error("Error:", error);
          const errorMessage = {
            role: "assistant",
            content: "오류가 발생했습니다.",
          };
          setMessages((prev) => [...prev, errorMessage]);
        } finally {
          setIsLoading(false);
        }
        return;
      }

      // 다음 질문으로 이동
      setCurrentStep(stepConfig.nextStep);
      const nextQuestion = stepQuestions[stepConfig.nextStep];
      const aiMessage = {
        role: "assistant",
        content: nextQuestion.content,
      };
      setMessages((prev) => [...prev, aiMessage]);
      return;
    }

    // 일반 메시지 처리
    setIsLoading(true);
    try {
      let contextMessage = currentInput;
      let shouldResetToInitial = false;

      if (selectedOption) {
        if (
          selectedOption === 1 ||
          selectedOption.toString().startsWith("1-")
        ) {
          contextMessage = `[중고 PC 가격 확인 상담]\n사용자 질문: ${currentInput}`;
          shouldResetToInitial = true;
        } else if (
          selectedOption === 2 ||
          selectedOption.toString().startsWith("2-")
        ) {
          contextMessage = `[PC 부품 호환성 검사 상담]
            사용자 질문: ${currentInput}

            간결하게 답변해주세요:
            1. 첫 줄에 "호환됩니다" 또는 "호환되지 않습니다"로 시작
            2. 핵심 이유만 1-2줄로 설명`;
          shouldResetToInitial = true;
        }
      }

      const response = await api.post("/api/chat/message", {
        message: contextMessage,
      });

      const aiMessage = { role: "assistant", content: response.data.reply };
      setMessages((prev) => [...prev, aiMessage]);

      // 1번, 2번 옵션 사용 시 처음으로 돌아가기
      if (shouldResetToInitial) {
        setTimeout(() => {
          setMessages((prev) => [...prev, initialMessage]);
        }, 500);

        // 초기화
        setCurrentStep(null);
        setSelectedOption(null);
        setCollectedData({});
      }
    } catch (error) {
      console.error("Error:", error);
      const errorMessage = {
        role: "assistant",
        content: "오류가 발생했습니다.",
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <>
      <button className="chatbot-button" onClick={toggleChat}>
        {isOpen ? "✕" : <img src={chatbot}></img>}
      </button>

      {isOpen && (
        <div className="chatbot-container">
          <div className="chatbot-header">
            <h3>AI ASSISTANT</h3>
          </div>

          <div className="chatbot-messages">
            {messages.map((msg, index) => (
              <div key={index}>
                <div className={`message ${msg.role}`}>{msg.content}</div>
                {msg.options && (
                  <div className="message-options">
                    {msg.options.map((option) => (
                      <button
                        key={option.id}
                        className="option-button"
                        onClick={() => handleOptionClick(option)}
                      >
                        {option.text}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {isLoading && <div className="message assistant">입력 중...</div>}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-input">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="메시지를 입력하세요..."
            />
            <button onClick={sendMessage} disabled={isLoading}>
              ➤
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default Chatbot;
