package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.UtilityMeter;

public interface UtilityMeterService extends CrudService<UtilityMeter> {
    UtilityMeter record(UtilityMeter meter);
}
