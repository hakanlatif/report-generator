package com.example.reportgenerator.test;

import lombok.Getter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

public class MockitoStaticExtension<T> implements BeforeEachCallback, AfterEachCallback {

    private final Class<T> classToMock;

    @Getter
    private MockedStatic<T> mockedStatic;

    public MockitoStaticExtension(Class<T> classToMock) {
        this.classToMock = classToMock;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        mockedStatic = mockStatic(classToMock);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

}
