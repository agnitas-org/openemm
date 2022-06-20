--	db: >column;
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--                                                                                                                                                                                                                                                                  --
--                                                                                                                                                                                                                                                                  --
--        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   --
--                                                                                                                                                                                                                                                                  --
--        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    --
--        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           --
--        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            --
--                                                                                                                                                                                                                                                                  --
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--
_now = date.date ()
_yday = _now.month * 100 + _now.day

--
-- Convert to number and validate parameter
function _tonum (s)
	local	rc
	
	if s ~= nil then
		rc = math.floor (tonumber (s))
		if rc < 1 then
			rc = nil
		end
	else
		rc = nil
	end
	return rc
end

--
-- call as [agnAGE column="..." position="..." length="..." modifier="..."]
-- column: the column to read the date to compare to from
-- position[optional]: return the result starting from position (starting at 1)
-- length[optional]: the maximum length for the return value
-- modifier[optional]: modify the age
function age (ctx, parm, cust)
	--
	-- Initialize the local storage if not set
	if ctx._setting == nil then
		ctx._setting = {opt = {}, result = {}}
	end

	local	result = nil

	--
	-- To avoid duplicate calculation of the age just
	-- recalculate if we have a new recipient
	if ctx._setting.customerID == nil or ctx._setting.customerID ~= ctx.customer_id or ctx._setting.result[parm.column] == nil then
		if ctx._setting.customerID == nil or ctx._setting.customerID ~= ctx.customer_id then
			ctx._setting.result = {}
			ctx._setting.customerID = ctx.customer_id
		end
		
		local	value = cust[parm.column:lower ()]
		
		if value ~= nil then
			typ = type (value)
			if typ == 'number' then
				result = value
			elseif typ == 'string' then
				result = tonumber (value)
			elseif typ == 'date' then
				local	ref = value.month * 100 + value.day
				
				result = _now.year - value.year
				if ref > _yday then
					result = result - 1
				end
			end
		end
		ctx._setting.customerID = ctx.customer_id
		ctx._setting.result[parm.column] = result
	else
		result = ctx._setting.result[parm.column]
	end
	if result == nil then
		result = ''
	elseif type (result) == 'number' then
		if parm.modifier then
			local	m = load ('return '..tostring (result)..' '..parm.modifier);
			
			if m ~= nil then
				local	n, rc = pcall (m)
		
				if n then
					result = rc
				end
			end
		end
		result = tostring (math.floor (result))
	end
	
	local	opt = ctx._setting.opt[parm]
	--
	-- If we had not parsed the parameter yet, do it once
	if opt == nil then
		opt = {length = _tonum (parm.length), position = _tonum (parm.position)}
		ctx._setting.opt[parm] = opt
	end
	
	if opt.length or opt.position then
		if opt.position ~= nil then
			if opt.position > result:len () then
				result = ''
			else
				result = result:sub (opt.position)
			end
		end
		if opt.length ~= nil then
			result = result:sub (1, opt.length)
		end
	end
	return result
end
