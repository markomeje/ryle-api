package com.ryle.service;
import com.ryle.data.CreditPlanEnum;
import com.ryle.document.UserCreditDocument;
import com.ryle.repository.UserCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditService {
    private final UserCreditRepository userCreditRepository;

    public void createInitialCredits(String clerkId) {
        String plan = String.valueOf(CreditPlanEnum.BASIC);
        UserCreditDocument userCredits = UserCreditDocument.builder()
           .clerkId(clerkId)
           .credits(5)
           .plan(plan)
           .build();

        userCreditRepository.save(userCredits);
    }
}
