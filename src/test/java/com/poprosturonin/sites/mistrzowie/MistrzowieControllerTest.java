package com.poprosturonin.sites.mistrzowie;

import com.poprosturonin.utils.URLUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mistrzowie tests
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MistrzowieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnJson() throws Exception {
        mockMvc.perform(get("/mistrzowie/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("nextPage", matchesPattern(URLUtils.CUT_URL_PATTERN)));
        mockMvc.perform(get("/mistrzowie/page/2000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("nextPage", matchesPattern(URLUtils.CUT_URL_PATTERN)));
        mockMvc.perform(get("/mistrzowie/715370"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void shouldReturnEmptyPage() throws Exception {
        mockMvc.perform(get("/mistrzowie/page/-1"))
                .andExpect(status().isNotFound());
    }
}