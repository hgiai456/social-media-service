package com.giaidev.fileservice.mapper;

import com.giaidev.fileservice.dto.FileInfo;
import com.giaidev.fileservice.entity.FileMgmt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMgmtMapper {
    @Mapping(target = "id", source = "name")
    FileMgmt toFileMgmt(FileInfo fileInfo);
}
