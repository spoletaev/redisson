package org.redisson;

import io.netty.util.concurrent.Future;

import java.util.Collections;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.redisson.core.RScript;

import com.lambdaworks.redis.RedisException;

public class RedissonScriptTest extends BaseTest {

    @Test
    public void testEval() {
        RScript script = redisson.getScript();
        List<Object> res = script.eval("return {1,2,3.3333,'foo',nil,'bar'}", RScript.ReturnType.MULTI, Collections.emptyList());
        MatcherAssert.assertThat(res, Matchers.<Object>contains(1L, 2L, 3L, "foo"));
    }

    @Test
    public void testEvalAsync() {
        RScript script = redisson.getScript();
        Future<List<Object>> res = script.evalAsync("return {1,2,3.3333,'foo',nil,'bar'}", RScript.ReturnType.MULTI, Collections.emptyList());
        MatcherAssert.assertThat(res.awaitUninterruptibly().getNow(), Matchers.<Object>contains(1L, 2L, 3L, "foo"));
    }

    @Test
    public void testScriptExists() {
        RScript s = redisson.getScript();
        String r = s.scriptLoad("return redis.call('get', 'foo')");
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);
        
        List<Boolean> r1 = s.scriptExists(r);
        Assert.assertEquals(1, r1.size());
        Assert.assertTrue(r1.get(0));

        s.scriptFlush();
        
        List<Boolean> r2 = s.scriptExists(r);
        Assert.assertEquals(1, r2.size());
        Assert.assertFalse(r2.get(0));
    }
    
    @Test
    public void testScriptFlush() {
        redisson.getBucket("foo").set("bar");
        String r = redisson.getScript().scriptLoad("return redis.call('get', 'foo')");
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);
        String r1 = redisson.getScript().evalSha("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        Assert.assertEquals("bar", r1);
        String r2 = redisson.getScript().scriptFlush();
        Assert.assertEquals("OK", r2);
        try {
            redisson.getScript().evalSha("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        } catch (Exception e) {
            Assert.assertEquals(RedisException.class, e.getClass());
        }
    }
    
    @Test
    public void testScriptLoad() {
        redisson.getBucket("foo").set("bar");
        String r = redisson.getScript().scriptLoad("return redis.call('get', 'foo')");
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);
        String r1 = redisson.getScript().evalSha("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        Assert.assertEquals("bar", r1);
    }
    
    @Test
    public void testScriptLoadAsync() {
        redisson.getBucket("foo").set("bar");
        Future<String> r = redisson.getScript().scriptLoadAsync("return redis.call('get', 'foo')");
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r.awaitUninterruptibly().getNow());
        String r1 = redisson.getScript().evalSha("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        Assert.assertEquals("bar", r1);
    }
    
    @Test
    public void testEvalSha() {
        redisson.getBucket("foo").set("bar");
        String r = redisson.getScript().eval("return redis.call('get', 'foo')", RScript.ReturnType.VALUE);
        Assert.assertEquals("bar", r);
        String r1 = redisson.getScript().evalSha("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        Assert.assertEquals("bar", r1);
    }
    
    @Test
    public void testEvalshaAsync() {
        redisson.getBucket("foo").set("bar");
        String r = redisson.getScript().eval("return redis.call('get', 'foo')", RScript.ReturnType.VALUE);
        Assert.assertEquals("bar", r);
        Future<Object> r1 = redisson.getScript().evalShaAsync("282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList());
        Assert.assertEquals("bar", r1.awaitUninterruptibly().getNow());
    }

    
}
