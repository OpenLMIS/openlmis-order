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

import static org.openlmis.fulfillment.i18n.MessageKeys.CLASS_NOT_FOUND;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openlmis.fulfillment.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JasperReportsViewService {

  static final String PARAM_DATASOURCE = "datasource";
  
  @Autowired
  private DataSource replicationDataSource;

  /**
   * Generate a report based on the Jasper template.
   * Create compiled report from bytes from Template entity, and use compiled report to fill in data
   * and export to desired format.
   *
   * @param jasperTemplate template that will be used to generate a report
   * @param params  map of parameters
   * @return data of generated report
   */
  public byte[] generateReport(Template jasperTemplate, Map<String, Object> params) {

    JasperReport jasperReport = getReportFromTemplateData(jasperTemplate);

    try {
      JasperPrint jasperPrint;
      if (params.containsKey(PARAM_DATASOURCE)) {
        jasperPrint = JasperFillManager.fillReport(jasperReport, params,
            new JRBeanCollectionDataSource((List) params.get(PARAM_DATASOURCE)));
      } else {
        try (Connection connection = replicationDataSource.getConnection()) {
          jasperPrint = JasperFillManager.fillReport(jasperReport, params,
              connection);
        }
      }

      return prepareReport(jasperPrint, params);
    } catch (Exception e) {
      throw new JasperReportViewException(e, ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE,
          e.getMessage());
    }
  }

  /**
   * Get (compiled) Jasper report from Jasper template.
   *
   * @param jasperTemplate template
   * @return Jasper report
   */
  private JasperReport getReportFromTemplateData(Template jasperTemplate) {

    try (ObjectInputStream inputStream =
        new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {

      return (JasperReport) inputStream.readObject();
    } catch (IOException ex) {
      throw new JasperReportViewException(ex, ERROR_IO, ex.getMessage());
    } catch (ClassNotFoundException ex) {
      throw new JasperReportViewException(ex, CLASS_NOT_FOUND, JasperReport.class.getName());
    }
  }

  private byte[] prepareReport(JasperPrint jasperPrint, Map<String, Object> params)
      throws JRException {
    if (null != params.get("format")) {
      return getJasperExporter((String) params.get("format"), jasperPrint).exportReport();
    }

    return getJasperExporter("pdf", jasperPrint).exportReport();
  }

  private JasperExporter getJasperExporter(String keyParam, JasperPrint jasperPrint) {
    switch (keyParam) {
      case "pdf":
        return new JasperPdfExporter(jasperPrint);
      case "csv":
        return new JasperCsvExporter(jasperPrint);
      case "xls":
        return new JasperXlsExporter(jasperPrint);
      case "html":
        return new JasperHtmlExporter(jasperPrint);
      default:
        throw new IllegalArgumentException(keyParam);
    }
  }
}
