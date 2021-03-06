package com.example.demo.src.board;

import com.example.demo.src.board.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class BoardDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public List<GetBoardRes> getBoards() {
        String getBoardQuery = "select boardIdx, profileImgUrl,userNickname'userNickname',boardImgUrl\n" +
                "       ,concat('좋아요 ',(select count(BL.board_id) from Boardlike BL where BL.board_id=boardIdx),'개')'likeCnt',\n" +
                "       userNickname'boarduserNickname',boardDescription,\n" +
                "       concat('댓글 ',((select count(ReComment) from ReComment where ReComment.board_id=Board.boardIdx )+\n" +
                "        (select count(Comment) from Comment where Comment.board_id=Board.boardIdx)),'개 모두보기')'chatCnt',case\n" +
                "                   when YEAR(boardCreated)<YEAR(now())\n" +
                "                           then concat(YEAR(boardCreated),'년 ',MONTH(boardCreated),'월 ',DAY(boardCreated),'일')\n" +
                "                    when YEAR(boardCreated)=YEAR(now()) then\n" +
                "                        case\n" +
                "                              when (TIMESTAMPDIFF(DAY,boardCreated,now()))>7\n" +
                "                                then concat(month(boardCreated),'월 ',DAY(boardCreated),'일')\n" +
                "                            when TIMESTAMPDIFF(minute,boardCreated,now())<1\n" +
                "                               then concat(TIMESTAMPDIFF(second,boardCreated,now()))\n" +
                "                           when TIMESTAMPDIFF(hour,boardCreated,now())>24\n" +
                "                                then concat(TIMESTAMPDIFF(DAY,boardCreated,now()),'일 전')\n" +
                "                            when TIMESTAMPDIFF(hour,boardCreated,now())<1\n" +
                "                                then concat(TIMESTAMPDIFF(minute,boardCreated,now()),'분 전')\n" +
                "                            when TIMESTAMPDIFF(hour,boardCreated,now())<24\n" +
                "                                then concat(TIMESTAMPDIFF(hour,boardCreated,now()),'시간 전')\n" +
                "                            else concat(TIMESTAMPDIFF(second,boardCreated,now()),'초 전')\n" +
                "                        end end as boardTime from Board join BoardImg on Board.boardIdx = board_id\n" +
                "                            join User on Board.user_id=User.userIdx\n" +
                "                            left join Follow on Follow.follow_Id=UserIdx\n" +
                "where boardImgindex=1";
        return this.jdbcTemplate.query(getBoardQuery,
                (rs,rowNum) -> new GetBoardRes(
                        rs.getLong("boardIdx"),
                        rs.getString("profileImgUrl"),
                        rs.getString("userNickname"),
                        rs.getString("boardImgUrl"),
                        rs.getString("likeCnt"),
                        rs.getString("boarduserNickname"),
                        rs.getString("boardDescription"),
                        rs.getString("chatCnt"),
                        rs.getString("boardTime")
                ));
    }

    public List<GetBoardRes> getBoardsByBoardIdx(Long boardIdx) {
        String getBoardsByBoardIdxQuery = "select boardIdx, profileImgUrl,userNickname,boardImgUrl,boardDescription," +
                "case" +
                "    when YEAR(boardCreated)<YEAR(now())" +
                "           then concat(YEAR(boardCreated),'년 ',MONTH(boardCreated),'월 ',DAY(boardCreated),'일')" +
                "    when YEAR(boardCreated)=YEAR(now()) then" +
                "        case" +
                "              when (TIMESTAMPDIFF(DAY,boardCreated,now()))>7" +
                "                then concat(month(boardCreated),'월 ',DAY(boardCreated),'일')" +
                "            when TIMESTAMPDIFF(minute,boardCreated,now())<1" +
                "                then concat(TIMESTAMPDIFF(second,boardCreated,now()))" +
                "            when TIMESTAMPDIFF(hour,boardCreated,now())>24" +
                "                then concat(TIMESTAMPDIFF(DAY,boardCreated,now()),'일 전')" +
                "            when TIMESTAMPDIFF(hour,boardCreated,now())<1" +
                "                then concat(TIMESTAMPDIFF(minute,boardCreated,now()),'분 전')" +
                "            when TIMESTAMPDIFF(hour,boardCreated,now())<24" +
                "                then concat(TIMESTAMPDIFF(hour,boardCreated,now()),'시간 전')" +
                "            else concat(TIMESTAMPDIFF(second,boardCreated,now()),'초 전')" +
                "        end end as 'boardTime' from Board join BoardImg on Board.boardIdx = board_id join User on Board.user_id=User.userIdx where boardImgindex=1";
        Long getBoardParams=boardIdx;
        return this.jdbcTemplate.query(getBoardsByBoardIdxQuery,
                (rs,rowNum) -> new GetBoardRes(
                        rs.getLong("boardIdx"),
                        rs.getString("profileImgUrl"),
                        rs.getString("userNickname"),
                        rs.getString("boardImgUrl"),
                        rs.getString("likeCnt"),
                        rs.getString("boarduserNickname"),
                        rs.getString("boardDescription"),
                        rs.getString("chatCnt"),
                        rs.getString("boardTime")
                ),getBoardParams);
    }


    public List<GetBoardCommentRes> getBoardComment(Long boardIdx) {
        String getBoardsCommentByBoardIdxQuery="select CommentUser.profileimgUrl'commentprofileImgUrl',CommentUser.userNickName'commentuserNickName',comment'comment',\n" +
                "       case when TIMESTAMPDIFF(WEEK,CommentCreated_date,now())>1 " +
                "                   then concat(TIMESTAMPDIFF(WEEK,CommentCreated_date,now()),'주 전')\n" +
                "            when TIMESTAMPDIFF(hour, CommentCreated_date,now())>24\n" +
                "                   then concat(TIMESTAMPDIFF(DAY,CommentCreated_date,now()),'일 전')\n" +
                "            when TIMESTAMPDIFF(hour, CommentCreated_date,now())<1\n" +
                "                   then concat(TIMESTAMPDIFF(minute,CommentCreated_date,now()),'분 전')\n" +
                "            else concat(TIMESTAMPDIFF(hour,CommentCreated_date,now()),'시간 전')\n" +
                "           end as 'commentDate',\n" +
                "       (select profileimgUrl from User where User.userIdx=ReComment.user_id and reCommentStatus='TRUE' " +
                "order by reCommentCreated_date desc limit 1)'recommentprofileImgUrl',\n" +
                "       (select userNickName from User where User.userIdx=ReComment.user_id and reCommentStatus='TRUE' " +
                "order by reCommentCreated_date desc limit 1)'recommentuserNickName',\n" +
                "       (select ReComment from User where User.userIdx=ReComment.user_id and reCommentStatus='TRUE' " +
                "order by reCommentCreated_date desc limit 1)'reComment',\n" +
                "       case " +
                "           when TIMESTAMPDIFF(WEEK,ReCommentCreated_date,now())>1\n" +
                "                then concat(TIMESTAMPDIFF(WEEK,ReCommentCreated_date,now()),'주 전')\n" +
                "            when TIMESTAMPDIFF(hour, ReCommentCreated_date,now())>24\n" +
                "                then concat(TIMESTAMPDIFF(DAY,ReCommentCreated_date,now()),'일 전')\n" +
                "            when TIMESTAMPDIFF(hour, ReCommentCreated_date,now())<1\n" +
                "                then concat(TIMESTAMPDIFF(minute,ReCommentCreated_date,now()),'분 전')\n" +
                "            else concat(TIMESTAMPDIFF(hour,ReCommentCreated_date,now()),'시간 전')\n" +
                "           end as 'reCommentDate'\n" +
                "from Comment left join Board on Comment.board_id=Board.boardIdx\n" +
                "             left join ReComment on ReComment.comment_id=Comment.commentIdx " +
                "               join User CommentUser on CommentUser.userIdx =Comment.user_id\n" +
                "where Board.boardIdx=? and commentStatus='TRUE' order by CommentCreated_date desc";
        Long getBoardCommentParams=boardIdx;

        return this.jdbcTemplate.query(getBoardsCommentByBoardIdxQuery,
                (rs,rowNum)-> new GetBoardCommentRes(
                        rs.getString("commentprofileImgUrl"),
                        rs.getString("commentuserNickName"),
                        rs.getString("comment"),
                        rs.getString("commentDate"),
                        rs.getString("recommentprofileImgUrl"),
                        rs.getString("recommentuserNickName"),
                        rs.getString("reComment"),
                        rs.getString("reCommentDate")),getBoardCommentParams);
    }

    public List<GetBoardFollowRes> getBoardFollow(Long userIdx) {
        String getBoardFollowByuserIdxQuery="select boardIdx, profileImgUrl,userNickname'userNickname',boardImgUrl\n" +
                "       ,concat('좋아요 ',(select count(BL.board_id) from Boardlike BL where BL.board_id=boardIdx),'개')'likeCnt',\n" +
                "       userNickname'boarduserNickname',boardDescription,\n" +
                "       concat('댓글 ',((select count(ReComment) from ReComment where ReComment.board_id=Board.boardIdx )+\n" +
                "        (select count(Comment) from Comment where Comment.board_id=Board.boardIdx)),'개 모두보기')'chatCnt',case\n" +
                "                   when YEAR(boardCreated)<YEAR(now())\n" +
                "                           then concat(YEAR(boardCreated),'년 ',MONTH(boardCreated),'월 ',DAY(boardCreated),'일')\n" +
                "                    when YEAR(boardCreated)=YEAR(now()) then\n" +
                "                        case\n" +
                "                            when TIMESTAMPDIFF(hour,boardCreated,now())<1\n" +
                "                                then concat(TIMESTAMPDIFF(minute,boardCreated,now()),'분 전')\n" +
                "                            when TIMESTAMPDIFF(hour,boardCreated,now())<24\n" +
                "                                then concat(TIMESTAMPDIFF(hour,boardCreated,now()),'시간 전')\n" +
                "                              when (TIMESTAMPDIFF(DAY,boardCreated,now()))>7\n" +
                "                                then concat(month(boardCreated),'월 ',DAY(boardCreated),'일')\n" +
                "                            when TIMESTAMPDIFF(minute,boardCreated,now())<1\n" +
                "                               then concat(TIMESTAMPDIFF(second,boardCreated,now()))\n" +
                "                           when TIMESTAMPDIFF(hour,boardCreated,now())>24\n" +
                "                                then concat(TIMESTAMPDIFF(DAY,boardCreated,now()),'일 전')\n" +
                "                            else concat(TIMESTAMPDIFF(second,boardCreated,now()),'초 전')\n" +
                "                        end end as boardTime from Board join BoardImg on Board.boardIdx = board_id\n" +
                "                            join User on Board.user_id=User.userIdx\n" +
                "                            left join Follow on Follow.follow_Id=UserIdx\n" +
                "where boardImgindex=1 and Follow.user_id=?\n";
        Long getBoardFollowParams=userIdx;
        return this.jdbcTemplate.query(getBoardFollowByuserIdxQuery,
                (rs,rowNum) ->new GetBoardFollowRes(
                        rs.getLong("boardIdx"),
                        rs.getString("profileImgUrl"),
                        rs.getString("userNickname"),
                        rs.getString("boardImgUrl"),
                        rs.getString("likeCnt"),
                        rs.getString("boarduserNickname"),
                        rs.getString("boardDescription"),
                        rs.getString("chatCnt"),
                        rs.getString("boardTime")),getBoardFollowParams
                );


    }
    public List<GetBoardRecommentRes> getBoardRecomment(Long commentIdx) {
        String getBoardRecommentQuery = "select profileImgUrl,userNickname,recomment, " +
                "case when TIMESTAMPDIFF(WEEK,ReCommentCreated_date,now())>1 then concat(TIMESTAMPDIFF(WEEK,ReCommentCreated_date,now()),'주 전')" +
                "when TIMESTAMPDIFF(hour, ReCommentCreated_date,now())>=24 then '1일 전' " +
                "when TIMESTAMPDIFF(hour, ReCommentCreated_date,now())>48 " +
                "then if(TIMESTAMPDIFF(DAY,ReCommentCreated_date,now())>7, date_format(ReCommentCreated_date,'%Y-%m-%d'), " +
                "concat(TIMESTAMPDIFF(DAY,ReCommentCreated_date,now()),'일 전')) " +
                "when TIMESTAMPDIFF(hour, ReCommentCreated_date,now())<1 " +
                "then concat(TIMESTAMPDIFF(minute,ReCommentCreated_date,now()),'분 전')" +
                "else concat(TIMESTAMPDIFF(hour,ReCommentCreated_date,now()),'시간 전') " +
                "end as 'reCommentDate' from User " +
                "join ReComment on user_id=userIdx " +
                "where comment_id=? and reCommentStatus='TRUE' order by reCommentCreated_date desc;";
        Long getBoardRecommentParmas=commentIdx;

        return this.jdbcTemplate.query(getBoardRecommentQuery,
                (rs,rowNum)->new GetBoardRecommentRes(
                        rs.getString("profileImgUrl"),
                        rs.getString("userNickname"),
                        rs.getString("recomment"),
                        rs.getString("reCommentDate")
                ),getBoardRecommentParmas
                );

    }

    public int createComment(PostBoardCommentReq postBoardCommentReq) {
        String createBoardCommentQuery="insert into Comment(board_id ,user_id,comment) values(?,?,?)";
        Object[] createBoardCommentParams = new Object[]{
                postBoardCommentReq.getBoard_id(),postBoardCommentReq.getUser_id(),postBoardCommentReq.getComment()
        };
        return this.jdbcTemplate.update(createBoardCommentQuery,createBoardCommentParams);
    }

    public int createRecomment(PostBoardRecommentReq postBoardReommentReq) {
        String createBoardRecommentQuery ="insert into ReComment(board_id,comment_id,user_id,recomment) values(?,?,?,?)";
        Object[] createBoardRecommentParams = new Object[]{
                postBoardReommentReq.getBoard_id(),postBoardReommentReq.getComment_id(),postBoardReommentReq.getUser_id(),postBoardReommentReq.getRecomment()
        };
        return this.jdbcTemplate.update(createBoardRecommentQuery,createBoardRecommentParams);
    }

    public int createBoardLike(PostBoardLikeReq postBoardLikeReq) {
        String createBoardLikeQuery ="insert into Boardlike(board_id,user_id) values(?,?)";
        Object[] createBoardLikeParams = new Object[]{
                postBoardLikeReq.getBoard_id(),postBoardLikeReq.getUser_id()
        };
        return this.jdbcTemplate.update(createBoardLikeQuery,createBoardLikeParams);
    }

    public int createCommentLike(PostCommentLikeReq postCommentLikeReq) {
        String createCommentLikeQuery = "insert into CommentLike(comment_id,user_id) values(?,?)";
        Object[] createCommentLikeParams = new Object[]{
                postCommentLikeReq.getComment_id(),postCommentLikeReq.getUser_id()
        };
        return this.jdbcTemplate.update(createCommentLikeQuery,createCommentLikeParams);
    }

    public int createRecommentLike(PostRecommentLikeReq postRecommentLikeReq) {
        String createRecommentLikeQuery ="insert into ReCommentLike(comment_id,recomment_id,user_id) values(?,?,?)";
        Object[] createRecommentLikeParams = new Object[]{
                postRecommentLikeReq.getComment_id(), postRecommentLikeReq.getRecomment_id(),postRecommentLikeReq.getUser_id()
        };
        return this.jdbcTemplate.update(createRecommentLikeQuery,createRecommentLikeParams);
    }

    public int deleteComment(DeleteCommentReq deleteCommentReq) {
        String deleteCommentQuery = "update Comment set commentStatus='FALSE' where user_id=? and commentIdx=?";
        Object[] deleteCommentParams =new Object[]{
                deleteCommentReq.getUser_id(),deleteCommentReq.getCommentIdx()
        };

        return this.jdbcTemplate.update(deleteCommentQuery,deleteCommentParams);
    }
    public int deleteReComment(DeleteCommentReq deleteCommentReq) {
        String deleteReCommentQuery="update ReComment set reCommentStatus='FALSE' where comment_id=?";
        Long deleteReCommentParams=deleteCommentReq.getCommentIdx();

        return this.jdbcTemplate.update(deleteReCommentQuery,deleteReCommentParams);
    }

    public int postBoard(PostBoardReq postBoardReq) {
        String postBoardQuery= "insert into Board(user_id,positonInfo_id,boardDescription) values(?,?,?);";
        Object[] postBoardParams=new Object[]{postBoardReq.getUser_id(),postBoardReq.getPositionInfo_id(),postBoardReq.getBoardDescription()};
        return this.jdbcTemplate.update(postBoardQuery,postBoardParams);
    }
    public int postBoardImg(PostBoardReq postBoardReq) {
        String postBoardImgQuery ="insert into BoardImg(user_id, board_id,boardImgUrl) values(?,last_insert_id(),?);";
        Object[] postBoardImgParmas=new Object[]{
                postBoardReq.getUser_id(),postBoardReq.getBoardImgUrl()
        };
        return this.jdbcTemplate.update(postBoardImgQuery,postBoardImgParmas);
    }



}
