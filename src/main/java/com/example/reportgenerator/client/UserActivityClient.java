package com.example.reportgenerator.client;

import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityResponse;
import jakarta.annotation.Nonnull;
import java.time.ZonedDateTime;

public interface UserActivityClient {

    UserActivityResponse fetchReportsWithRetry(@Nonnull ZonedDateTime dateFrom, @Nonnull ZonedDateTime dateTo) throws ServiceException;

}
