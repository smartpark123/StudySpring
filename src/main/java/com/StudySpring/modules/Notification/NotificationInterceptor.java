package com.StudySpring.modules.Notification;

import com.StudySpring.modules.account.Account;
import com.StudySpring.modules.account.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //modelAndView를 사용하는 경우, 리다이렉트 뷰가 아닌경우 authentication이 있어야하며 Principal 타입이 UserAccount여야함
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount) {
            Account account = ((UserAccount)authentication.getPrincipal()).getAccount(); //account 꺼냄
            long count = notificationRepository.countByAccountAndChecked(account,false); //알림 개수
            modelAndView.addObject("hasNotification", count > 0); //개수가 0보다 크면 true
        }
    }

    // 리다이렉트 뷰인지 확인
    private boolean isRedirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}
