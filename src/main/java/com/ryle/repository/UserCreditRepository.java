package com.ryle.repository;
import com.ryle.document.UserCreditDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCreditRepository extends MongoRepository<UserCreditDocument, String> {
}
