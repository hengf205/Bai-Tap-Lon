package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService extends CrudService<Room> {
    Page<Room> search(String keyword, Long buildingId, RoomStatus status, RoomType roomType, Pageable pageable);
}
