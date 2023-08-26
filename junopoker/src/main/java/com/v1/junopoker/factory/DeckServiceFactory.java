package com.v1.junopoker.factory;

import com.v1.junopoker.service.DeckService;
import org.springframework.stereotype.Service;

@Service
public class DeckServiceFactory {
    public DeckService createDeckService() {
        return new DeckService();
    }
}
