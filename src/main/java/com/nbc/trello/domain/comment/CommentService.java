package com.nbc.trello.domain.comment;

import com.nbc.trello.domain.card.Card;
import com.nbc.trello.domain.card.CardRepository;
import com.nbc.trello.domain.comment.dto.CommentRequest;
import com.nbc.trello.domain.comment.dto.CommentResponse;
import com.nbc.trello.domain.participants.ParticipantsRepository;
import com.nbc.trello.domain.todo.TodoRepository;
import com.nbc.trello.domain.user.User;
import com.nbc.trello.domain.user.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final TodoRepository todoRepository;
    private final ParticipantsRepository participantsRepository;
    private final UserRepository userRepository;

    public CommentResponse createComment(String email, Long boardId, Long todoId, Long cardId,
        CommentRequest request) {
        // 유저 확인
        User user = findUserBy(email);
        // 참여자 확인
        validateParticipants(boardId, user);
        // 카드 확인
        Card card = findCardBy(cardId);
        // to-do 가 보드에 있는지
        validateTodoExistInBoard(boardId, todoId);
        // 카드가 to-do 에 있는지
        validateCardExistInTodo(todoId, cardId);

        Comment comment = new Comment(request.getContent(), user, card);

        Comment saveComment = commentRepository.save(comment);

        return new CommentResponse(email, saveComment.getContent());
    }

    public CommentResponse updateComment(String email, Long boardId, Long todoId, Long cardId,
        Long commentId, CommentRequest request) {

        User user = findUserBy(email);

        validateParticipants(boardId, user);

        Card card = findCardBy(cardId);

        validateTodoExistInBoard(boardId, todoId);

        validateCardExistInTodo(todoId, cardId);

        Comment comment = findCommentBy(commentId);

        userMatchValidate(comment, user);

        comment.update(request);

        return new CommentResponse(comment);
    }

    public String deleteComment(String email, Long boardId, Long todoId, Long cardId,
        Long commentId) {

        User user = findUserBy(email);

        validateParticipants(boardId, user);

        Card card = findCardBy(cardId);

        validateTodoExistInBoard(boardId, todoId);

        validateCardExistInTodo(todoId, cardId);

        Comment comment = findCommentBy(commentId);

        userMatchValidate(comment, user);

        commentRepository.delete(comment);

        return "댓글 삭제 성공!";
    }

    private User findUserBy(String email) {
        return userRepository.findByEmail(email).orElseThrow(
            () -> new EntityNotFoundException("User 가 존재하지 않습니다."));
    }

    private void validateParticipants(Long boardId, User user) {
        if (!participantsRepository.existsByBoardIdAndUserId(boardId, user.getId())) {
            throw new EntityExistsException("참여자가 존재하지 않습니다.");
        }
    }

    private void validateCardExistInTodo(Long todoId, Long cardId) {
        if (!cardRepository.existsByIdAndTodoId(cardId, todoId)) {
            throw new EntityExistsException("Todo 에 Card 가 존재하지 않습니다.");
        }
    }

    private void validateTodoExistInBoard(Long boardId, Long todoId) {
        if (!todoRepository.existsByIdAndBoardId(todoId, boardId)) {
            throw new EntityExistsException("Board 에 Todo 가 존재하지 않습니다.");
        }
    }

    private Card findCardBy(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(
            () -> new EntityNotFoundException("Card 가 존재하지 않습니다.")
        );
    }

    private Comment findCommentBy(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
            () -> new EntityNotFoundException("댓글이 존재하지 않습니다.")
        );
    }

    private void userMatchValidate(Comment comment, User user) {
        if (!comment.getUser().equals(user)) {
            throw new EntityExistsException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
    }
}
