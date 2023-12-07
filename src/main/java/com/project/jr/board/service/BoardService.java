package com.project.jr.board.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.project.jr.board.model.BoardDTO;
import com.project.jr.board.model.PageDTO;
import com.project.jr.board.repository.BoardDAO;
import com.project.jr.like.model.BoardLikeDTO;
import com.project.jr.like.repository.BoardLikeDAO;

/**
 * 게시판 서비스
 * @author eugene
 *
 */
@Service
public class BoardService {

	@Autowired
	private BoardDAO bdao;

	@Autowired
	private BoardLikeDAO ldao;

	@Autowired
	private MessageService mserv;

	/**
	 * 게시판 목록을 반환하는 메소드
	 * @param pdto
	 * @return
	 */
	public List<BoardDTO> boardList(PageDTO pdto) {

		List<BoardDTO> list = bdao.list(pdto);

		// 제목가공
		for (BoardDTO b : list) {

			if (b.getBoardTitle().length() > 20) {
				b.setBoardTitle(b.getBoardTitle().substring(0, 20) + "...");
			}

			b.setBoardTitle(b.getBoardTitle().replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;"));
		}

		return list;
	}

	/**
	 * 게시글 추가 결과를 반환하는 메소드
	 * @param dto
	 * @param resp
	 * @return
	 */
	public String boardAdd(BoardDTO dto, HttpServletResponse resp) {
		int result = bdao.add(dto);
		if (result == 1) {
			return "redirect:/board/list.do";
		} else {
			mserv.redirectWithMessage(resp, "작성에 실패했습니다.");
			return null;
		}
	}
	
	/**
	 * 조회수를 카운팅하는 메소드
	 * @param session
	 * @param boardSeq
	 */
	public void readCounting(HttpSession session, String boardSeq) {
		if ((session.getAttribute("read") == null
				|| session.getAttribute("read").toString().equals("n")) 
				&& session.getAttribute(boardSeq) == null) {
			
			bdao.updateReadcount(boardSeq);
			
			//새로고침시 조회수 카운팅 방지
			session.setAttribute("read", "y");
			//같은 글 여러번 카운팅 방지
			session.setAttribute(boardSeq, "y");
		}
	}
	
	/**
	 * 게시글 상세정보를 반환하는 메소드
	 * @param boardSeq
	 * @param pdto
	 * @return
	 */
	public BoardDTO getDetail(String boardSeq, PageDTO pdto) {
		//게시글 정보 가져오기
		BoardDTO dto = bdao.get(boardSeq);
		
		//제목/내용 태그방지, 공백, 개행처리
		dto.setBoardTitle(dto.getBoardTitle().replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;"));
		dto.setBoardContent(dto.getBoardContent().replace("<", "&lt;").replace(">", "&gt;").replace("\r\n", "<br>").replace(" ", "&nbsp;"));
		
		//내용으로 검색 > 검색어 강조
		if (pdto.getSearch() != null && pdto.getSearch().equals("y") && pdto.getColumn().equals("boardContent")) {
			dto.setBoardContent(dto.getBoardContent().replace(pdto.getWord(), "<span style=\"background-color:#0dcaf0; color:#FFF;\">" + pdto.getWord() + "</span>"));
		}
		return dto;
	}
	
	/**
	 * 좋아요 클릭 여부를 확인하는 메소드
	 * @param model
	 * @param session
	 * @param ldto
	 */
	public void checkLiked(Model model, HttpSession session, BoardLikeDTO ldto) {
		//좋아요 누른 글인지 확인
		//BoardLike 테이블 불러와 > boardSeq랑 id같이 보내서 비교해
		// 있어? > 좋아요글이야 > "like", "y"
		// 없어? > 좋아요글 아니야 > 아무짓안함
		//int result = ldao.liked();
		if (session.getAttribute("id") != null) {
			
			ldto.setId(session.getAttribute("id").toString());
			if (ldao.isLiked(ldto) == 1) {
				model.addAttribute("liked", "y");
			}
			
		}
	}
	
	/**
	 * 게시글 삭제 결과를 반환하는 메소드
	 * @param boardSeq
	 * @param resp
	 * @return
	 */
	public String boardDel(String boardSeq, HttpServletResponse resp) {
		int result = bdao.del(boardSeq);
		
		if (result == 1) {
			return "redirect:/board/list.do";
		} else {
			mserv.redirectWithMessage(resp, "삭제에 실패했습니다.");
			return null;
		}
	}

	/**
	 * 게시글 수정을 위해 게시글 제목과 내용을 반환하는 메소드
	 * @param boardSeq
	 * @return
	 */
	public BoardDTO getEditDetail(String boardSeq) {
		return bdao.get(boardSeq);
	}
	
	/**
	 * 게시글 수정 결과를 반환하는 메소드
	 * @param dto
	 * @param resp
	 * @return
	 */
	public String boardEdit(BoardDTO dto, HttpServletResponse resp) {
		int result = bdao.edit(dto);
		
		if (result == 1) {
			return String.format("redirect:/board/detail.do?boardSeq=%d", dto.getBoardSeq());
		} else {
			mserv.redirectWithMessage(resp, "수정에 실패했습니다.");
			return null;
		}
	}

	public BoardDTO getBoardDetail(String boardSeq) {
		return bdao.get(boardSeq);
	}

}
