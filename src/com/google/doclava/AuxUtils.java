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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuxUtils {
  private static final int TYPE_METHOD = 0;
  private static final int TYPE_FIELD = 1;
  private static final int TYPE_PARAM = 2;
  private static final int TYPE_RETURN = 3;

  public static TagInfo[] fieldAuxTags(FieldInfo field) {
    if (hasSuppress(field)) return TagInfo.EMPTY_ARRAY;
    return auxTags(TYPE_FIELD, field.annotations());
  }

  public static TagInfo[] methodAuxTags(MethodInfo method) {
    if (hasSuppress(method)) return TagInfo.EMPTY_ARRAY;
    return auxTags(TYPE_METHOD, method.annotations());
  }

  public static TagInfo[] paramAuxTags(MethodInfo method, ParameterInfo param) {
    if (hasSuppress(method)) return TagInfo.EMPTY_ARRAY;
    if (hasSuppress(param.annotations())) return TagInfo.EMPTY_ARRAY;
    return auxTags(TYPE_PARAM, param.annotations());
  }

  public static TagInfo[] returnAuxTags(MethodInfo method) {
    if (hasSuppress(method)) return TagInfo.EMPTY_ARRAY;
    return auxTags(TYPE_RETURN, method.annotations());
  }

  private static TagInfo[] auxTags(int type, List<AnnotationInstanceInfo> annotations) {
    ArrayList<TagInfo> tags = new ArrayList<>();
    for (AnnotationInstanceInfo annotation : annotations) {
      ParsedTagInfo[] docTags = ParsedTagInfo.EMPTY_ARRAY;
      switch (type) {
        case TYPE_METHOD:
        case TYPE_FIELD:
          docTags = annotation.type().comment().memberDocTags();
          break;
        case TYPE_PARAM:
          docTags = annotation.type().comment().paramDocTags();
          break;
        case TYPE_RETURN:
          docTags = annotation.type().comment().returnDocTags();
          break;
      }
      for (ParsedTagInfo docTag : docTags) {
        tags.add(docTag);
      }

      // IntDef below belongs in return docs, not in method body
      if (type == TYPE_METHOD) continue;

      if (annotation.type().qualifiedName().equals("android.annotation.IntRange")
          || annotation.type().qualifiedName().equals("android.annotation.FloatRange")) {
        String from = null;
        String to = null;
        for (AnnotationValueInfo val : annotation.elementValues()) {
          switch (val.element().name()) {
            case "from": from = String.valueOf(val.value()); break;
            case "to": to = String.valueOf(val.value()); break;
          }
        }
        if (from != null || to != null) {
          tags.add(new RangeTagInfo(SourcePositionInfo.UNKNOWN, from, to));
        }
      }

      for (AnnotationInstanceInfo inner : annotation.type().annotations()) {
        if (inner.type().qualifiedName().equals("android.annotation.IntDef")) {
          ArrayList<AnnotationValueInfo> prefixes = null;
          ArrayList<AnnotationValueInfo> values = null;
          boolean flag = false;

          for (AnnotationValueInfo val : inner.elementValues()) {
            switch (val.element().name()) {
              case "prefix": prefixes = (ArrayList<AnnotationValueInfo>) val.value(); break;
              case "value": values = (ArrayList<AnnotationValueInfo>) val.value(); break;
              case "flag": flag = Boolean.parseBoolean(String.valueOf(val.value())); break;
            }
          }

          // Sadly we can only generate docs when told about a prefix
          if (prefixes == null || prefixes.isEmpty()) continue;

          final ClassInfo clazz = annotation.type().containingClass();
          final HashMap<String, FieldInfo> candidates = new HashMap<>();
          for (FieldInfo field : clazz.fields()) {
            if (field.isHiddenOrRemoved())
              continue;
            for (AnnotationValueInfo prefix : prefixes) {
              if (field.name().startsWith(String.valueOf(prefix.value()))) {
                candidates.put(String.valueOf(field.constantValue()), field);
              }
            }
          }

          ArrayList<TagInfo> valueTags = new ArrayList<>();
          for (AnnotationValueInfo value : values) {
            final String expected = String.valueOf(value.value());
            final FieldInfo field = candidates.get(expected);
            if (field != null) {
              valueTags.add(new ParsedTagInfo("", "",
                  "{@link " + clazz.qualifiedName() + "#" + field.name() + "}", null,
                  SourcePositionInfo.UNKNOWN));
            }
          }

          if (!valueTags.isEmpty()) {
            tags.add(new IntDefTagInfo(SourcePositionInfo.UNKNOWN, flag,
                valueTags.toArray(TagInfo.getArray(valueTags.size()))));
          }
        }
      }
    }
    return tags.toArray(TagInfo.getArray(tags.size()));
  }

  private static boolean hasSuppress(MemberInfo member) {
    return hasSuppress(member.annotations())
        || hasSuppress(member.containingClass().annotations());
  }

  private static boolean hasSuppress(List<AnnotationInstanceInfo> annotations) {
    for (AnnotationInstanceInfo annotation : annotations) {
      if (annotation.type().qualifiedName().equals("android.annotation.SuppressAutoDoc")) {
        return true;
      }
    }
    return false;
  }
}
