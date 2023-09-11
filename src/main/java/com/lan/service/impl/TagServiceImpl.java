package com.lan.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lan.mapper.TagMapper;
import com.lan.model.domain.Tag;
import com.lan.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author lan
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
