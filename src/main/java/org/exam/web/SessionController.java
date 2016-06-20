package org.exam.web;

import org.exam.domain.doc.SessionInfo;
import org.exam.repository.mongo.MongoSessionInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by on 16/6/20.
 */
@Controller
@RequestMapping("session")
public class SessionController {
    private static final LogoutHandler logoutHandler = new SecurityContextLogoutHandler();


    @Autowired
    private MongoSessionInfoRepo mongoSessionInfoRepo;

    @RequestMapping("list")
    public String list(Model model) {
        Iterable<SessionInfo> items = mongoSessionInfoRepo.findAll();
        model.addAttribute("items", items);
        return "session/list";
    }

    @RequestMapping("logout")
    public String logout(String sid) {
        SessionInfo info = mongoSessionInfoRepo.findBySid(sid);
        info.setExpired(true);
        mongoSessionInfoRepo.save(info);
        return "session/list";
    }
}
