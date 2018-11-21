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

package org.openlmis.fulfillment.service.shipment;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.FileColumnBuilder;
import org.openlmis.fulfillment.FileTemplateBuilder;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.service.FileTemplateService;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ShipmentMessageHandlerTest {

  private static final String NEW_MESSAGE_CSV = "new-message.csv";

  @Mock
  FileTemplateService templateService;

  @Mock
  ShipmentCsvFileParser shipmentParser;

  @Mock
  ShipmentObjectBuilderService shipmentPersistenceHelper;

  @Mock
  MessageChannel errorChannel;

  @Mock
  MessageChannel archiveChannel;

  @Mock
  ApplicationContext context;

  @Mock
  ShipmentService shipmentService;

  @InjectMocks
  ShipmentMessageHandler messageHandler;

  FileTemplate template;

  @Before
  public void setup() {
    FileTemplateBuilder templateBuilder = new FileTemplateBuilder();
    FileColumnBuilder columnBuilder = new FileColumnBuilder();

    FileColumn orderCode = columnBuilder.withKeyPath("orderCode").build();

    template = templateBuilder
        .withTemplateType(TemplateType.SHIPMENT)
        .withFileColumns(asList(orderCode))
        .build();

    when(templateService.getFileTemplate(TemplateType.SHIPMENT)).thenReturn(template);

    when(context.getBean("errorChannel")).thenReturn(errorChannel);
    when(context.getBean("archiveFtpChannel")).thenReturn(archiveChannel);
  }

  @Test
  public void shouldUseCachedFileTemplate() throws Exception {
    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);
    messageHandler.process(fileMessage);
    messageHandler.process(fileMessage);

    verify(templateService, atMost(1)).getFileTemplate(TemplateType.SHIPMENT);
  }

  @Test
  public void shouldSendFileToErrorChannelWhenThereIsParserError() throws Exception {
    when(shipmentParser.parse(any(), any())).thenThrow(new RuntimeException());

    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);
    verify(errorChannel).send(any());
  }

  @Test
  public void shouldSendFileToErrorChannelWhenErrorBuildingShipmentFile() throws Exception {
    doThrow(new RuntimeException()).when(shipmentPersistenceHelper).build(any(), any());

    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);
    verify(errorChannel).send(any());
  }

  @Test
  public void shouldSendFileToErrorChannelWhenErrorPersistingShipmentFile() throws Exception {
    when(shipmentParser.parse(any(), any())).thenReturn(createParsedData());
    when(shipmentPersistenceHelper.build(any(), any()))
        .thenReturn(new ShipmentDataBuilder().build());
    when(shipmentService.save(any())).thenThrow(new RuntimeException());
    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);
    verify(errorChannel).send(any());
    verify(archiveChannel, never()).send(any());
  }


  @Test
  public void shouldSaveFileToArchiveChannelWhenThereIsNoError() throws Exception {
    when(shipmentParser.parse(any(), any())).thenReturn(createParsedData());
    when(shipmentPersistenceHelper.build(any(), any()))
        .thenReturn(new ShipmentDataBuilder().build());

    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);
    verify(archiveChannel).send(any());
    verify(errorChannel, never()).send(any());
  }

  @Test
  public void shouldSaveShipmentWhenThereIsNoError() throws Exception {
    Shipment shipment = new ShipmentDataBuilder().build();
    when(shipmentPersistenceHelper.build(any(), any()))
        .thenReturn(shipment);
    when(shipmentParser.parse(any(), any())).thenReturn(createParsedData());
    when(shipmentService.save(shipment)).thenReturn(shipment);

    Message<File> fileMessage = MessageBuilder
        .withPayload(new File(NEW_MESSAGE_CSV)).build();

    messageHandler.process(fileMessage);

    verify(shipmentService).save(any());
  }

  private List<Object[]> createParsedData() {
    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList("O111", UUID.randomUUID().toString(), "1000").toArray());
    return parsedData;
  }
}