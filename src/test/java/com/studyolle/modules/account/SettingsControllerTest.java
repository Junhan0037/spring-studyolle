package com.studyolle.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.AccountService;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import com.studyolle.modules.tag.TagForm;
import com.studyolle.modules.tag.TagRepository;
import com.studyolle.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studyolle.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll(); // @WithAccount에서 생성한 계정을 지워준다
    }

//    @BeforeEach
//    void beforeEach() {
//        SignUpForm signUpForm = new SignUpForm();
//        signUpForm.setNickname("junhan");
//        signUpForm.setEmail("junhan@email.com");
//        signUpForm.setPassword("123456789");
//        accountService.processNewAccount(signUpForm);
//    }
//
//    @WithUserDetails(value = "junhan", setupBefore = TestExecutionEvent.TEST_EXECUTION) // 버그가 있어서 설정이 안됨 (@BeforeEach 다음, 테스트코드 실행 전에 실행)
    @WithAccount("junhan") // 이름에 해당하는 계정을 만든다
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("junhan")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account junhan = accountRepository.findByNickname("junhan");
        assertEquals(bio, junhan.getBio());
    }

    @WithAccount("junhan")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account junhan = accountRepository.findByNickname("junhan");
        assertNull(junhan.getBio());
    }

    @WithAccount("junhan")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("junhan")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account junhan = accountRepository.findByNickname("junhan");
        assertTrue(passwordEncoder.matches("123456789", junhan.getPassword()));
    }

    @WithAccount("junhan")
    @DisplayName("패스워드 수정 - 입력값 에러")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "111111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("junhan")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("junhan")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("junhan").getTags().contains(newTag)); // @Transactional 필요
    }

    @WithAccount("junhan")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account junhan = accountRepository.findByNickname("junhan");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(junhan, newTag);

        assertTrue(junhan.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(junhan.getTags().contains(newTag));
    }

    @WithAccount("junhan")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

//    @WithAccount("junhan")
//    @DisplayName("계정의 지역 정보 추가")
//    @Test
//    void addZone() throws Exception {
//        ZoneForm zoneForm = new ZoneForm();
//        zoneForm.setZoneName(testZone.toString());
//
//        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(zoneForm))
//                .with(csrf()))
//                .andExpect(status().isOk());
//
//        Account junhan = accountRepository.findByNickname("junhan");
//        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
//        assertTrue(junhan.getZones().contains(zone));
//    }
//
//    @WithAccount("junhan")
//    @DisplayName("계정의 지역 정보 삭제")
//    @Test
//    void removeZone() throws Exception {
//        Account junhan = accountRepository.findByNickname("junhan");
//        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
//        accountService.addZone(junhan, zone);
//
//        ZoneForm zoneForm = new ZoneForm();
//        zoneForm.setZoneName(testZone.toString());
//
//        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(zoneForm))
//                .with(csrf()))
//                .andExpect(status().isOk());
//
//        assertFalse(junhan.getZones().contains(zone));
//    }

}