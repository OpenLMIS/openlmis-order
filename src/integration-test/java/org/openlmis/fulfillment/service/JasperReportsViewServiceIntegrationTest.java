/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.fulfillment.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@DirtiesContext
@RunWith(SpringRunner.class)
public class JasperReportsViewServiceIntegrationTest {

  private static final String EMPTY_REPORT_RESOURCE = "/empty-report.jrxml";
  private static final int HIKARI_DEFAULT_POOL_SIZE = 10;
  static final String PARAM_DATASOURCE = "datasource";

  @Autowired
  private JasperReportsViewService service;

  @Test
  public void generateReportShouldNotThrowErrorAfterPrintingReport10Times()
      throws JRException, IOException, JasperReportViewException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(getEmptyReport());
    out.flush();

    Template template = new Template();
    template.setData(bos.toByteArray());
    Map<String, Object> params = new HashMap<>();
    params.put("format", "pdf");

    for (int i = 0; i < HIKARI_DEFAULT_POOL_SIZE + 1; i++) {
      service.generateReport(template, params);
    }
  }

  @Test
  public void generateReportShouldNotThrowErrorForDatasourceParam()
      throws JRException, IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(getEmptyReport());
    out.flush();

    Template template = new Template();
    template.setData(bos.toByteArray());
    Map<String, Object> params = new HashMap<>();
    params.put(PARAM_DATASOURCE, new ArrayList<>());

    service.generateReport(template, params);
  }

  @Test(expected = JasperReportViewException.class)
  public void shouldThrowJasperReportViewExceptionForUnknownFormat()
      throws JRException, IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(getEmptyReport());
    out.flush();

    Template template = new Template();
    template.setData(bos.toByteArray());
    Map<String, Object> params = new HashMap<>();
    params.put("format", "odt");

    service.generateReport(template, params);
  }

  private JasperReport getEmptyReport() throws JRException {
    return JasperCompileManager
        .compileReport(getClass().getResourceAsStream(EMPTY_REPORT_RESOURCE));
  }
}
