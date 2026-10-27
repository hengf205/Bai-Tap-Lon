package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.UtilityMeter;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.UtilityMeterRepository;
import com.example.qlkytucxa.service.UtilityMeterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilityMeterServiceImpl extends AbstractCrudService<UtilityMeter> implements UtilityMeterService {
    private final UtilityMeterRepository repository;
    public UtilityMeterServiceImpl(UtilityMeterRepository repository) { super(repository, "Chỉ số điện nước"); this.repository = repository; }
    @Override @Transactional public UtilityMeter record(UtilityMeter meter) {
        if (meter.getElectricityNew().compareTo(meter.getElectricityOld()) < 0 || meter.getWaterNew().compareTo(meter.getWaterOld()) < 0) {
            throw new InvalidOperationException("Chỉ số mới không được nhỏ hơn chỉ số cũ");
        }
        UtilityMeter previous = repository.findTopByRoomIdOrderByRecordingDateDesc(meter.getRoom().getId()).orElse(null);
        if (previous != null) {
            if (!meter.getRecordingDate().isAfter(previous.getRecordingDate())) throw new InvalidOperationException("Ngày ghi mới phải sau lần ghi trước");
            if (meter.getElectricityOld().compareTo(previous.getElectricityNew()) != 0 || meter.getWaterOld().compareTo(previous.getWaterNew()) != 0) {
                throw new InvalidOperationException("Chỉ số cũ phải khớp chỉ số mới của kỳ trước");
            }
        }
        return repository.save(meter);
    }
}
