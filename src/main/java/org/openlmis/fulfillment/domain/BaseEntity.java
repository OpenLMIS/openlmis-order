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

package org.openlmis.fulfillment.domain;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.UUID;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.openlmis.util.View;

@MappedSuperclass
@EqualsAndHashCode
public abstract class BaseEntity implements Identifiable {
  public static final String ID = "id";
  
  static final String TEXT_COLUMN_DEFINITION = "text";
  static final String UUID_TYPE = "pg-uuid";

  static final int LINE_ITEMS_BATCH_SIZE = 300;

  @Id
  @GeneratedValue(generator = "uuid-gen")
  @GenericGenerator(name = "uuid-gen",
      strategy = "org.openlmis.fulfillment.util.ConditionalUuidGenerator")
  @JsonView(View.BasicInformation.class)
  @Type(type = UUID_TYPE)
  @Getter
  @Setter
  protected UUID id;
}
