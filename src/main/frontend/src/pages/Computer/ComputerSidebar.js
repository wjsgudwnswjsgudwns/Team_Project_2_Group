import { Link } from "react-router-dom";
import "./ComputerSidebar.css"; // 새로운 CSS 파일 임포트

function ComputerSidebar() {
  const menuItems = [
    { name: "CPU", path: "/cpu" },
    { name: "쿨러", path: "/cooler" },
    { name: "메인보드", path: "/mainboard" },
    { name: "메모리", path: "/memory" },
    { name: "그래픽카드", path: "/gpu" },
    { name: "디스크 (SSD/HDD)", path: "/disk" },
    { name: "케이스", path: "/case" },
    { name: "파워", path: "/power" },
  ];

  return (
    <div className="computer-sidebar">
      <h3 className="sidebar-title">PC 주요 구성</h3>
      <div className="sidebar-menu">
        {menuItems.map((item) => (
          <Link key={item.name} to={item.path} className="sidebar-menu-item">
            {item.name}
          </Link>
        ))}
      </div>
    </div>
  );
}

export default ComputerSidebar;
