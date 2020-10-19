package io.xiatiao.catbin.catbin;

import org.springframework.data.repository.CrudRepository;

public interface CustomCodeRepository extends CrudRepository<Code, Integer> {
    Code findByDigest(String digest);
}
