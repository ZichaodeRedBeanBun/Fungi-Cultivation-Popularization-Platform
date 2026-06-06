package cn.edu.seig.MhWeb.controller.user;

import cn.edu.seig.MhWeb.model.vo.CategoryItemVO;
import cn.edu.seig.MhWeb.model.vo.NationalTrendVO;
import cn.edu.seig.MhWeb.model.vo.ProvinceCompareVO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IMushroomDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/data")
@CrossOrigin
public class MushroomDataController {

    @Autowired
    private IMushroomDataService mushroomDataService;

    @GetMapping("/categories")
    public Result<List<CategoryItemVO>> getCategories() {
        return mushroomDataService.getAllCategories();
    }

    @GetMapping("/national-trend/{categoryId}")
    public Result<NationalTrendVO> getNationalTrend(@PathVariable Long categoryId) {
        return mushroomDataService.getNationalTrend(categoryId);
    }

    @GetMapping("/province-compare/{categoryId}/{year}")
    public Result<ProvinceCompareVO> getProvinceCompare(@PathVariable Long categoryId, @PathVariable Integer year) {
        return mushroomDataService.getProvinceCompare(categoryId, year);
    }
}