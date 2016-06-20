package org.exam.web;

import org.exam.domain.entity.User;
import org.exam.repository.jpa.JpaUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * Created on 15/1/7.
 */
@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private JpaUserRepo jpaUserRepo;

    //@PreAuthorize("hasAuthority('USER_QUERY')")
    @RequestMapping("/list")
    public String list(Model model, Pageable pageable) {
        Page<User> page = jpaUserRepo.findAll(pageable);
        model.addAttribute("page", page);
        return "user/list";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String add(Model model, HttpSession session) {
        model.addAttribute("action", "add");
        return "user/save";
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(User user) {
        jpaUserRepo.save(user);
        return "redirect:list";
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String update(Model model, Long id) {
        User user = jpaUserRepo.findOne(id);
        model.addAttribute("action", "update");
        model.addAttribute("user", user);
        return "user/save";
    }

    @RequestMapping(value = "delete")
    public String delete(Long id) {
        jpaUserRepo.delete(id);
        return "redirect:list";
    }
}
