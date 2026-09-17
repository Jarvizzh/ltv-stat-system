package com.ltv.stat.service.flicknovel;

import com.ltv.stat.dto.flicknovel.FlicknovelRelationDto;
import com.ltv.stat.entity.FlicknovelRelation;
import com.ltv.stat.repository.FlicknovelRelationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FlicknovelRelationSyncTest {

    @Autowired
    private FlicknovelApiService flicknovelApiService;

    @Autowired
    private FlicknovelRelationRepository flicknovelRelationRepository;

    private final String testRelationId = "TEST_REL_" + System.currentTimeMillis();

    @AfterEach
    public void tearDown() {
        Optional<FlicknovelRelation> rel = flicknovelRelationRepository.findByRelationId(testRelationId);
        rel.ifPresent(flicknovelRelationRepository::delete);
    }

    @Test
    public void testSaveOrUpdateRelationAndQuery() {
        FlicknovelRelationDto dto = new FlicknovelRelationDto();
        dto.setRelationId(testRelationId);
        dto.setDeviceId("test_dev_12345");
        dto.setPromotionId("358620015697768448");
        dto.setPromotionCode("PROMO_CODE_ABC");
        dto.setAdId("ad_id_999");
        dto.setAdsetId("adset_id_888");
        dto.setCampaignId("camp_id_777");
        dto.setAdAccountId("acc_id_666");
        dto.setMediaChannel("Facebook");
        dto.setPlatform("h5");
        dto.setAppId("2000019");
        dto.setRelationBeginTime("1789586051"); // 对应某一时间戳

        // 1. 测试持久化落库
        int saved = flicknovelApiService.saveOrUpdateRelations(Collections.singletonList(dto));
        assertEquals(1, saved);

        // 2. 查库断言各字段完整性
        Optional<FlicknovelRelation> opt = flicknovelRelationRepository.findByRelationId(testRelationId);
        assertTrue(opt.isPresent(), "染色记录应成功入库");
        FlicknovelRelation entity = opt.get();

        assertEquals(testRelationId, entity.getRelationId());
        assertEquals("test_dev_12345", entity.getDeviceId());
        assertEquals("358620015697768448", entity.getPromotionId());
        assertEquals("PROMO_CODE_ABC", entity.getPromotionCode());
        assertEquals("ad_id_999", entity.getAdId());
        assertEquals("adset_id_888", entity.getAdsetId());
        assertEquals("camp_id_777", entity.getCampaignId());
        assertEquals("acc_id_666", entity.getAdAccountId());
        assertEquals("Facebook", entity.getMediaChannel());
        assertEquals("h5", entity.getPlatform());
        assertEquals("2000019", entity.getAppId());
        assertEquals(1789586051L, entity.getRelationBeginTimestamp());
        assertNotNull(entity.getRelationBeginTimeBj(), "北京时间应自动解析");
        assertNotNull(entity.getRelationBeginTimeEt(), "美东时间应自动解析");
        assertNotNull(entity.getRelationBeginDateEt(), "美东日期应自动解析");
        assertNotNull(entity.getRawPayload(), "原始报文应保存");

        // 3. 测试根据 deviceId 查询最早染色
        Optional<FlicknovelRelation> byDev = flicknovelRelationRepository.findFirstByDeviceIdOrderByRelationBeginTimeBjAsc("test_dev_12345");
        assertTrue(byDev.isPresent());
        assertEquals(testRelationId, byDev.get().getRelationId());

        // 4. 测试幂等更新
        dto.setMediaChannel("TikTok");
        int updated = flicknovelApiService.saveOrUpdateRelations(Collections.singletonList(dto));
        assertEquals(1, updated);

        Optional<FlicknovelRelation> updatedOpt = flicknovelRelationRepository.findByRelationId(testRelationId);
        assertTrue(updatedOpt.isPresent());
        assertEquals("TikTok", updatedOpt.get().getMediaChannel(), "渠道应成功更新为 TikTok");
    }
}
