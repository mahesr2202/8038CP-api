package com.example.form8038cp.form.pdf;

import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormScheduleA;

import java.io.IOException;
import java.util.List;

public interface ScheduleAPdfService {
    byte[] generateScheduleA(
            FormPartII partII,
            FormPartIII partIII,
            List<FormScheduleA> rows) throws IOException;
}
