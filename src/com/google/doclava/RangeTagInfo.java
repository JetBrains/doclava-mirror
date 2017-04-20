/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.doclava;

import com.google.clearsilver.jsilver.data.Data;

public class RangeTagInfo extends TagInfo {
  public static final RangeTagInfo[] EMPTY_ARRAY = new RangeTagInfo[0];

  public static RangeTagInfo[] getArray(int size) {
      return size == 0 ? EMPTY_ARRAY : new RangeTagInfo[size];
  }

  private String mFrom;
  private String mTo;

  RangeTagInfo(SourcePositionInfo position, String from, String to) {
    super("@range", "@range", "", position);
    mFrom = from;
    mTo = to;
  }

  @Override
  public void makeHDF(Data data, String base) {
    super.makeHDF(data, base);
    if (mFrom != null) {
      data.setValue(base + ".from", mFrom);
    }
    if (mTo != null) {
      data.setValue(base + ".to", mTo);
    }
  }
}
