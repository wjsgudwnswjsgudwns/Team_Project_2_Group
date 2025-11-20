import "./Footer.css";

function Footer() {
  const logo = process.env.REACT_APP_LOGO_URL;

  return (
    <div className="footer">
      <div className="footer-container">
        <div className="footer-info">
          <div>주소 (우) 서울특별시 마포구 신촌로 176</div>
          <div>사업자번호: 123-45-56789</div>
          <div>대표 : 전형준, 최민수</div>
        </div>
      </div>
      <div className="footer-container2">
        <img src={logo} alt="로고" className="footer-logo"></img>
        <div className="footer-copyright">
          Copyright © OptiCore Co., Ltd. All Rights Reserved.
        </div>
      </div>
    </div>
  );
}

export default Footer;
