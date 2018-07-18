package top.arieslee.myblog.controller;

import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.arieslee.myblog.constant.WebConstant;
import top.arieslee.myblog.exception.TipException;
import top.arieslee.myblog.modal.BO.CommentBo;
import top.arieslee.myblog.modal.VO.CommentVo;
import top.arieslee.myblog.modal.VO.ContentVo;
import top.arieslee.myblog.service.ICommentService;
import top.arieslee.myblog.service.IContentService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName IndexController
 * @Description 首页controller
 * @Author Aries
 * @Date 2018/7/6 15:38
 * @Version 1.0
 **/
@Controller
public class IndexController extends BaseController {
    //日志对象
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    //注入相关业务层接口
    @Autowired
    private IContentService contentService;

    @Autowired
    private ICommentService commentService;

    /**
     * @Author: Aries
     * @Description : 初始主页访问
     * @Date : 16:20 2018/7/10
     * @Param [request, limit:每页文章数量]
     **/
    @GetMapping(value = {"/", "index"})
    public String index(HttpServletRequest request, @RequestParam(value = "limit", defaultValue = "1") int limit) {
        //调用文章分页接口处理
        return this.index(request, 1, limit);
    }

    /**
     * @Author: Aries
     * @Description :文章分页控制
     * @Date : 16:57 2018/7/10
     * @Param [request, p:当前页码, limit:每页文章数量]
     **/
    @GetMapping(value = {"page/{p}", "page/{p}.html"})
    public String index(HttpServletRequest request, @PathVariable("p") int p, @RequestParam(value = "limit", defaultValue = "1") int limit) {
        //判断页码是否合法,不合法置为1
        p = p < 1 || p > WebConstant.MAX_PAGE ? 1 : p;
        //调用业务层接口，获取mybatis分页插件执行结果
        PageInfo articles = contentService.getContent(p, limit);
        request.setAttribute("articles", articles);
        super.title(request, "第" + p + "页");
        return super.rend("index");
    }

    /**
     * @return java.lang.String
     * @Description : 根据文章id或者文章slug获取文章
     * @Date : 17:05 2018/7/13
     * @Param [request, cid]
     **/
    @GetMapping(value = "article/{cid}")
    public String getArticle(HttpServletRequest request, @PathVariable("cid") String cid) {
        //调用业务层接口处理
        ContentVo contentVo = contentService.getContent(cid);
        //文章不存在或者文章保存为草稿，转发到404页面
        if (contentVo == null || contentVo.getStatus().equals("draft")) {
            return super.page404();
        }
        //设置文章属性
        request.setAttribute("article", contentVo);
        //设置评论组件
        commentSet(request, contentVo);
        //页面跳转
        return super.rend("page");
    }

    /**
     * @Description : 配置评论组件
     **/
    public void commentSet(HttpServletRequest request, ContentVo contentVo) {
        //文章允许评论
        if (contentVo.getAllowComment()) {
            //获取到当前页
            String cp = request.getParameter("cp");
            if (StringUtils.isBlank(cp)) {
                cp = "1";
            }
            //返回当前页数
            request.setAttribute("cp", cp);
            //执行评论分页查询
            PageInfo<CommentBo> pageInfo = commentService.getComment(Integer.valueOf(contentVo.getCid()), Integer.valueOf(cp), 6);
            request.setAttribute("comments", pageInfo);
        }
    }

    /**
     * @Description 评论操作
     **/
    @PostMapping("comment")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public void publishComment(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam Integer cid, @RequestParam Integer coid,
                               @RequestParam String author, @RequestParam String mail,
                               @RequestParam String url, @RequestParam String text, @RequestParam String _csrf_token) {

    }
}
