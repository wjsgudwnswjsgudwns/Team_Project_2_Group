// chatbotResponses.js

export const initialMessage = {
  role: "assistant",
  content: "안녕하세요! OPTICORE AI 어시스턴트입니다.\n무엇을 도와드릴까요?",
  options: [
    { id: 1, text: "중고 PC 가격 확인" },
    { id: 2, text: "PC 부품 호환성 검사" },
    { id: 3, text: "일반 상담" },
  ],
};

export const initialResponses = {
  1: {
    content:
      "중고 PC 가격 확인 서비스입니다.\n중고 PC의 가격이 합리적인지 판별해드립니다.\n어떤 방식으로 확인하시겠습니까?",
    options: [
      { id: "1-1", text: "전체 사양 입력하기" },
      { id: "1-2", text: "부품별로 입력하기" },
    ],
    useApi: false,
  },
  2: {
    content:
      "PC 부품 호환성 검사 서비스입니다. \n어떤 방식으로 검사하시겠습니까?",
    options: [
      { id: "2-1", text: "전체 부품 리스트 입력" },
      { id: "2-2", text: "특정 부품 조합 확인" },
    ],
    useApi: false,
  },
  3: {
    content:
      "일반 상담입니다.\nPC 관련하여 궁금하신 점을 자유롭게 질문해주세요!",
    useApi: true,
  },

  "1-1": {
    content:
      "전체 사양을 한 번에 입력해주세요.\n\n예시:\n- CPU: AMD 라이젠5-6세대 9600X\n- GPU: SAPPHIRE 라데온 RX 9060 XT PULSE OC D6 16GB\n- RAM: 삼성전자 DDR5-5600 (16GB)\n- SSD: 삼성전자 990 EVO Plus M.2 NVMe (1TB)\n- 쿨러: darkFlash NEBULA DN-360D ARGB (블랙)\n- 메인보드: ASUS PRIME B650M-A II 대원씨티에스\n- 케이스: darkFlash DS900 ARGB 강화유리 (화이트)\n- 파워: 마이크로닉스 Classic II 풀체인지 600W 80PLUS브론즈 ATX3.1\n- 가격: 100만원",
    useApi: false,
  },
  "1-2": {
    content:
      "부품별로 하나씩 입력하겠습니다.\n\n먼저 CPU 모델을 알려주세요.\n예시: Intel i7-10700K, AMD Ryzen 5 5600X",
    useApi: false,
  },

  "2-1": {
    content:
      "전체 부품 리스트를 입력해주세요.\n\n예시:\n- 메인보드: ASUS ROG STRIX B550-F\n- CPU: AMD Ryzen 5 5600X\n- RAM: Corsair 32GB DDR4-3200\n- GPU: RTX 3070\n- 파워: 750W 80+ Gold",
    useApi: false,
  },
  "2-2": {
    content:
      "확인하고 싶은 부품 조합을 알려주세요.\n\n예시:\n- 메인보드: MSI MAG X870 토마호크 WIFI\n- CPU: AMD 라이젠7-6세대 9800X3D",
    useApi: false,
  },
};

export const stepQuestions = {
  "1-2-gpu": {
    content:
      "그래픽카드(GPU) 모델을 알려주세요.\n예시: SAPPHIRE 라데온 RX 9060 XT PULSE OC D6 16GB",
    nextStep: "1-2-ram",
    dataKey: "gpu",
  },
  "1-2-ram": {
    content: "메모리(RAM) 모델을 알려주세요.\n예시: 삼성전자 DDR5-5600 (16GB)",
    nextStep: "1-2-ssd",
    dataKey: "ram",
  },
  "1-2-ssd": {
    content:
      "저장장치(SSD/HDD) 모델을 알려주세요.\n예시: 삼성전자 990 EVO Plus M.2 NVMe (1TB)",
    nextStep: "1-2-cooler",
    dataKey: "ssd",
  },
  "1-2-cooler": {
    content:
      "쿨러 모델을 알려주세요.\n예시: darkFlash NEBULA DN-360D ARGB (블랙)",
    nextStep: "1-2-mainboard",
    dataKey: "cooler",
  },
  "1-2-mainboard": {
    content: "메인보드 모델을 알려주세요.\n예시: ASUS PRIME B650M-A II",
    nextStep: "1-2-case",
    dataKey: "mainboard",
  },
  "1-2-case": {
    content: "케이스 모델을 알려주세요.\n예시: darkFlash DS900 ARGB (화이트)",
    nextStep: "1-2-power",
    dataKey: "case",
  },
  "1-2-power": {
    content:
      "파워 모델을 알려주세요.\n예시: 마이크로닉스 Classic II 600W 80PLUS",
    nextStep: "1-2-price",
    dataKey: "power",
  },
  "1-2-price": {
    content: "마지막으로 구매 가격을 알려주세요.\n예시: 100만원",
    nextStep: "complete",
    dataKey: "price",
  },
};
