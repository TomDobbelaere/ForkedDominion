TYPE=VIEW
query=select `dominion`.`card`.`cardName` AS `cardName` from `dominion`.`card` where (not(`dominion`.`card`.`cardName` in (select distinct `dominion`.`cardability`.`cardName` AS `cardName` from `dominion`.`cardability`)))
md5=2bbf3d80f6b6aa4c059baa1c857cb8fc
updatable=1
algorithm=0
definer_user=proton
definer_host=%
suid=1
with_check_option=0
revision=1
timestamp=2016-05-14 02:51:52
create-version=1
source=select `Card`.`cardName` AS `cardName` from `Card` where (not(`Card`.`cardName` in (select distinct `CardAbility`.`cardName` from `CardAbility`)))
