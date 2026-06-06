package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.model.dto.FeedbackDTO;
import cn.edu.seig.MhWeb.model.entity.Feedback;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 服务类
 *
 * @author su
 */
public interface IFeedbackService extends IService<Feedback> {

    // 获取反馈列表
    Result<PageResult<Feedback>> getAllFeedbacks(FeedbackDTO feedbackDTO);

    // 删除反馈
    Result deleteFeedback(Long feedbackId);

    // 批量删除反馈
    Result deleteFeedbacks(List<Long> feedbackIds);

    // 添加反馈
    Result addFeedback(String content);

}
