/* 20100517 l1jtw */

INSERT INTO `mobskill` VALUES ('91203', '0', '塞維斯(火燄投射)', '2', '50', '0', '0', '2', '0', '0', '0', '0', '0', '0', '15002', '0', '0', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91203', '1', '塞維斯-act01', '1', '100', '0', '0', '-1', '0', '0', '1', '0', '0', '15', '0', '0', '1', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91204', '0', '路西爾斯(火焰投射)', '2', '50', '0', '0', '2', '0', '0', '0', '0', '0', '0', '12058', '0', '0', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91204', '1', '路西爾斯-act01', '1', '100', '0', '0', '-1', '0', '0', '1', '0', '0', '15', '0', '0', '1', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91206', '0', '翼龍(奎斯特)(龍捲風)', '2', '30', '0', '0', '-3', '0', '0', '0', '0', '0', '0', '14068', '0', '0', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91206', '1', '翼龍(奎斯特)-特攻30', '1', '40', '0', '0', '-1', '0', '0', '1', '0', '0', '20', '0', '0', '30', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91207', '0', '食腐獸(奎斯特)(遠距離特殊攻擊)', '1', '40', '0', '0', '-3', '0', '0', '3', '0', '0', '20', '0', '0', '30', '0', '0', '0', '0');
INSERT INTO `mobskill` VALUES ('91207', '1', '食腐獸(奎斯特)-腐蝕毒液', '2', '60', '0', '0', '-8', '0', '0', '0', '0', '0', '0', '14042', '0', '0', '0', '0', '0', '0');

INSERT INTO `skills` VALUES ('15002', '塞維斯-火之矛', '0', '0', '5', '0', '0', '0', '0', '0', 'attack', '3', '25', '5', '3', '0', '0', '2', '64', '0', '8', '0', '0', '0', '', '18', '6791', '0', '0', '0', '0');

Update npc Set name = '塞維斯', nameid = '$5658' Where npcid = '91203';
Update npc Set name = '路西爾斯', nameid = '$1279' Where npcid = '91204';
Update npc Set name = '時空裂痕(奎斯特)', nameid = '$5656' Where npcid = '91205';
Update npc Set name = '翼龍(奎斯特)', nameid = '$4307' Where npcid = '91206';
Update npc Set name = '食腐獸(奎斯特)', nameid = '$4306' Where npcid = '91207';
