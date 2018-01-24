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

package org.openlmis.fulfillment.web.stockmanagement;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.fulfillment.domain.ShipmentLineItem;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class StockEventLineItemDto implements ShipmentLineItem.Exporter {
  private UUID orderableId;
  private UUID lotId;
  private Integer quantity;
  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
  private LocalDate occurredDate;
  private UUID destinationId;

  @Override
  @JsonIgnore
  public void setId(UUID id) {
    // nothing to do here
  }

  @Override
  @JsonIgnore
  public void setQuantityShipped(Long quantityShipped) {
    quantity = Math.toIntExact(quantityShipped);
  }
}