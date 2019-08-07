-- link: ![admin]%(rdir-domain)/form.do?agnCI=%(company-id)&agnFN=profile&agnUID=##AGNUID## agnPROFILE;
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

function profile (ctx, parm, cust)
	if ctx.anon and parm.anon ~= nil then
		return parm.anon
	end
	
	if ctx._result == nil then
		ctx._result = agn.strmap ('%(rdir_domain)/form.do?agnCI=%(company_id)&agnFN=profile&agnUID=##AGNUID##')
	end
	return ctx._result
end
