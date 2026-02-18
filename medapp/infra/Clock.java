package jp.suzuki.medapp.infra;

import java.time.LocalDateTime;

public interface Clock {
    LocalDateTime now();
}

