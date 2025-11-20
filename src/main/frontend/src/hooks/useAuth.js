import { useState, useEffect } from "react";
import api from "../api/axiosConfig";

export const useAuth = () => {
  const [currentUsername, setCurrentUsername] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCurrentUser();
  }, []);

  const fetchCurrentUser = async () => {
    try {
      const res = await api.get("/api/auth/me");
      setCurrentUsername(res.data.username);
    } catch (err) {
      console.error("사용자 정보 조회 실패:", err);
      setCurrentUsername(null);
    } finally {
      setLoading(false);
    }
  };

  return { currentUsername, loading };
};
