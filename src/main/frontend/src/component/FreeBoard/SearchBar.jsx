export default function SearchBar({
  searchType,
  searchKeyword,
  isSearching,
  onSearchTypeChange,
  onSearchKeywordChange,
  onSearch,
  onReset,
}) {
  const handleKeyPress = (e) => {
    if (e.key === "Enter") onSearch();
  };

  return (
    <div className="search-area">
      <select
        value={searchType}
        onChange={(e) => onSearchTypeChange(e.target.value)}
        className="search-select"
      >
        <option value="all">전체</option>
        <option value="title">제목</option>
        <option value="content">내용</option>
        <option value="author">작성자</option>
      </select>

      <input
        type="text"
        placeholder="검색어를 입력하세요"
        value={searchKeyword}
        onChange={(e) => onSearchKeywordChange(e.target.value)}
        onKeyPress={handleKeyPress}
        className="search-input"
      />

      <button onClick={onSearch} className="search-btn">
        검색
      </button>

      {isSearching && (
        <button onClick={onReset} className="reset-btn">
          초기화
        </button>
      )}
    </div>
  );
}
