import { useState } from "react";
import api from "../api/axiosConfig";

export const useSearch = () => {
  const [searchType, setSearchType] = useState("all");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  const handleSearch = async (onSearchComplete) => {
    if (!searchKeyword.trim()) {
      alert("검색어를 입력하세요.");
      return;
    }

    try {
      setIsSearching(true);

      const res = await api.get(
        `/api/freeboard/search?searchType=${searchType}&keyword=${encodeURIComponent(
          searchKeyword
        )}&page=0&size=10`
      );

      if (onSearchComplete) {
        onSearchComplete(res.data);
      }
    } catch (err) {
      alert("검색 실패: " + err.message);
    }
  };

  const resetSearch = (onReset) => {
    setIsSearching(false);
    setSearchKeyword("");
    if (onReset) {
      onReset();
    }
  };

  return {
    searchType,
    searchKeyword,
    isSearching,
    setSearchType,
    setSearchKeyword,
    setIsSearching,
    handleSearch,
    resetSearch,
  };
};
