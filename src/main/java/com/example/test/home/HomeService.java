package com.example.test.home;

import com.example.test.entity.CounselBoard;
import com.example.test.entity.FreeBoard;
import com.example.test.entity.InfoBoard;
import com.example.test.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    @Autowired
    private InfoBoardRepository infoBoardRepository;

    @Autowired
    private CounselBoardRepository counselBoardRepository;

    @Autowired
    private FreeCommentRepository freeCommentRepository;

    @Autowired
    private InfoCommentRepository infoCommentRepository;

    @Autowired
    private CounselCommentRepository counselCommentRepository;

    public Home2Dto getRecentPosts() {

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        List<FreeBoard> freeBoards = freeBoardRepository.findAll(pageRequest).getContent();
        List<InfoBoard> infoBoards = infoBoardRepository.findAll(pageRequest).getContent();
        List<CounselBoard> counselBoards = counselBoardRepository.findAll(pageRequest).getContent();

        return Home2Dto.builder()
                .infoBoardPosts(convertInfoBoardToDTOs(infoBoards))
                .freeBoardPosts(convertFreeBoardToDTOs(freeBoards))
                .counselBoardPosts(convertCounselBoardToDTOs(counselBoards))
                .build();

    }

    private List<BoardPreviewDTO> convertInfoBoardToDTOs(List<InfoBoard> boards) {
        return boards.stream()
                .map(board -> BoardPreviewDTO.builder()
                        .id(board.getId())
                        .title(board.getITitle())
                        .commentCount(infoCommentRepository.countByInfoBoard_Id(board.getId()))
                        .boardType("infoboard")
                        .build())
                .collect(Collectors.toList());
    }

    private List<BoardPreviewDTO> convertFreeBoardToDTOs(List<FreeBoard> boards) {
        return boards.stream()
                .map(board -> BoardPreviewDTO.builder()
                        .id(board.getId())
                        .title(board.getFTitle())
                        .commentCount(freeCommentRepository.countByFreeBoard_Id(board.getId()))
                        .boardType("freeboard")
                        .build())
                .collect(Collectors.toList());
    }

    private List<BoardPreviewDTO> convertCounselBoardToDTOs(List<CounselBoard> boards) {
        return boards.stream()
                .map(board -> BoardPreviewDTO.builder()
                        .id(board.getId())
                        .title(board.getCTitle())
                        .commentCount(counselCommentRepository.countByCounselBoard_Id(board.getId()))
                        .boardType("counselboard")
                        .build())
                .collect(Collectors.toList());
    }
}
