<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="objects" tilewidth="80" tileheight="80" tilecount="6" columns="0">
 <tile id="0" x="160" y="80" width="80" height="80">
  <properties>
   <property name="body_type" value="static"/>
   <property name="is_sensor" type="bool" value="true"/>
   <property name="sensor_behavior" value="hide"/>
  </properties>
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="9" x="0" y="1" width="79" height="68"/>
  </objectgroup>
 </tile>
 <tile id="1" x="240" y="80" width="80" height="80">
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="4" x="0" y="6" width="80" height="65">
    <properties>
     <property name="body_type" value="static"/>
     <property name="is_sensor" type="bool" value="true"/>
     <property name="sensor_behavior" value="hide"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="2" x="240" y="160" width="80" height="16">
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="17" y="0" width="46" height="14">
    <properties>
     <property name="body_type" value="static"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" x="16" y="304" width="32" height="32">
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="2" y="16" width="27" height="12">
    <properties>
     <property name="body_type" value="static"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="4" x="48" y="304" width="32" height="16">
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="3" y="0" width="26" height="15">
    <properties>
     <property name="body_type" value="static"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="6" x="80" y="304" width="16" height="16">
  <image width="352" height="336" source="tf_jungle_tileset.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="1" width="16" height="14">
    <properties>
     <property name="body_type" value="static"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
</tileset>
