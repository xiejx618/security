package org.exam.web;

import org.exam.domain.User;
import org.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by xin on 15/1/7.
 */
@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("list")
    public String list(Model model, Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        model.addAttribute("page", page);
        return "user/list";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("action", "add");
        return "user/save";
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(User user) {
        userRepository.save(user);
        return "redirect:list";
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String update(Model model, Long id) {
        User user = userRepository.findOne(id);
        model.addAttribute("action", "update");
        model.addAttribute("user", user);
        return "user/save";
    }

    @RequestMapping(value = "delete")
    public String delete(Long id) {
        userRepository.delete(id);
        return "redirect:list";
    }
}
