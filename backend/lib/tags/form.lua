-- require: name;
-- link: ![admin]%(rdir-domain)/form.action?agnCI=%(company-id)&agnFN=%(name)&agnUID=##AGNUID## Formular %(name);
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--                                                                                                                                                                                                                                                                  --
--                                                                                                                                                                                                                                                                  --
--        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   --
--                                                                                                                                                                                                                                                                  --
--        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    --
--        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           --
--        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            --
--                                                                                                                                                                                                                                                                  --
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

function form (ctx, parm, cust)
	if ctx.anon and parm.anon ~= nil then
		return parm.anon
	end
	
	if ctx._result == nil or ctx._result[parm] == nil then
		if ctx._result == nil then
			ctx._result = {}
		end
		
		local	map = {}
		
		map['param.name'] = parm.name
		ctx._result[parm] = agn.strmap ('%(rdir_domain)/form.action?agnCI=%(company_id)&agnFN=%(param.name)&agnUID=##AGNUID##', map)
	end
	return ctx._result[parm]
end
