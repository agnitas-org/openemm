-- db: >column;
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--                                                                                                                                                                                                                                                                  --
--                                                                                                                                                                                                                                                                  --
--        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   --
--                                                                                                                                                                                                                                                                  --
--        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    --
--        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           --
--        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            --
--                                                                                                                                                                                                                                                                  --
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--
function days_until (ctx, param, cust)
	if ctx.anon and param.anon ~= nil then
		return param.anon
	end
	local	target = cust (param.column)
	local	now = date.date ()
	
	if target ~= nil and target ~= null then
		target['hour'] = 0
		target['minute'] = 0
		target['second'] = 0
		now['hour'] = 0
		now['minute'] = 0
		now['second'] = 0
		return tostring (math.floor ((target:epoch () - now:epoch ()) / (24 * 60 * 60)))
	end
	return '0'
end
