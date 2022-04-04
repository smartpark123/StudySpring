package com.StudySpring.modules.account;


import com.StudySpring.modules.account.form.SignUpForm;
import com.StudySpring.modules.account.vaildator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up") //회원가입 제출 POST
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.processNewAccount(signUpForm);//Account 생성 작업
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if(account == null){ //조회된 account 값이 없다면
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)){ //받아온 토큰 값과 조회된 토큰 값이 다르면면
            model.addAttribute("error","wrong.token");
            return view;
        }

        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count()); //몇번째 유저인지 찍기 위해
        model.addAttribute("nickname",account.getNickname()); //가입된 유저 닉네임
        return view;

    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }
    @GetMapping("/resend-confirm-email") //이메일을 보낼 수 있는지 확인
    public String resendConfirmEmail(@CurrentAccount Account account, Model model){
        if(!account.canSendConfirmEmail()){ //1시간 이전에 생성되면 에러
            model.addAttribute("error", "인증 메일은 1시간에 한번만 전송 가능합니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentAccount Account account){


        Account accountToView = accountService.getAccount(nickname);

        model.addAttribute(accountToView);
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";

    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            //return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }

        accountService.login(account);
        return view;
    }

}
